package com.kartnegar.app.domain

data class DetectedItem(
    val type: ItemType,
    val normalizedValue: String,
    val formattedValue: String,
    val bank: IranianBank? = null,
    val expiryDate: String? = null,
    val checksumLabel: String,
    val confidenceHigh: Boolean = true
)

object FinancialExtractor {

    // Matches IR (or OCR misreads like 1R / lR / |R) followed by 24 digits with optional separators
    private val shebaRegex = Regex(
        """(?<![A-Za-z0-9])(?:[Ii1l|][Rr])\s*[-–—]?\s*(?:[0-9OoIl|]\s*[-–—.\u200C\u200F\u202A-\u202E]?){24}(?![0-9])"""
    )

    // Reversed RTL OCR case: 24 digits followed by IR
    private val reversedShebaRegex = Regex(
        """(?<![0-9])(?:[0-9]\s*[-–—.]?){24}\s*[Ii][Rr](?![A-Za-z0-9])"""
    )

    // Bare 24-digit sequence preceded by Sheba/IBAN keyword
    private val bareShebaContextRegex = Regex(
        """(?:شبا|شماره\s*شبا|IBAN|SHEBA|SHABA)\s*[:：\-]?\s*(?:IR)?\s*((?:[0-9]\s*[-–—.]?){24})(?![0-9])""",
        RegexOption.IGNORE_CASE
    )

    // 16 digits with optional spaces/dashes/dots (e.g. 4x4 groups or contiguous)
    private val cardRegex = Regex(
        """(?<![0-9])(?:[0-9]\s*[-–—.\u200C\u200F]?){16}(?![0-9])"""
    )

    // 4 groups of 4 digits on separate or same lines
    private val cardFourGroupsRegex = Regex(
        """(?<![0-9])([0-9]{4})[\s\-–—.]{1,4}([0-9]{4})[\s\-–—.]{1,4}([0-9]{4})[\s\-–—.]{1,4}([0-9]{4})(?![0-9])"""
    )

    // 10-digit National ID (optionally hyphenated like 001-234567-8 or spaced)
    private val nationalIdRegex = Regex(
        """(?<![0-9])(?:[0-9]{3}[\s\-–—]?[0-9]{6}[\s\-–—]?[0-9]|[0-9]{10})(?![0-9])"""
    )

    // Expiry date pattern: 01/05, 1405/08, 05/08, 12/28
    private val expiryRegex = Regex(
        """(?:انقضا|تاریخ|EXP|EXPIRY|VALID)?\s*[:：]?\s*\b((?:0[1-9]|1[0-2])\s*/\s*(?:[0-9]{2})|(?:0[1-9]|[1-9][0-9])\s*/\s*(?:0[1-9]|1[0-2]))\b""",
        RegexOption.IGNORE_CASE
    )

    private val nationalIdKeywords = listOf(
        "کد ملی", "کدملی", "شماره ملی", "شماره‌ملی", "ملی", "national", "melli", "nid", "code melli"
    )

    fun extractFromMultiplePasses(passes: List<String>): List<DetectedItem> {
        val merged = LinkedHashMap<String, DetectedItem>()
        for (text in passes) {
            if (text.isBlank()) continue
            for (item in extract(text)) {
                val existing = merged[item.normalizedValue]
                if (existing == null || (!existing.confidenceHigh && item.confidenceHigh) ||
                    (existing.expiryDate == null && item.expiryDate != null)
                ) {
                    merged[item.normalizedValue] = item
                }
            }
        }
        return merged.values.sortedWith(
            compareBy<DetectedItem> {
                when (it.type) {
                    ItemType.CARD -> 0
                    ItemType.SHEBA -> 1
                    ItemType.NATIONAL_ID -> 2
                }
            }.thenByDescending { it.confidenceHigh }
        )
    }

    fun extract(rawText: String): List<DetectedItem> {
        if (rawText.isBlank()) return emptyList()

        val normalized = FinancialValidators.normalizeDigits(rawText)
        val results = LinkedHashMap<String, DetectedItem>()
        val consumedRanges = mutableListOf<IntRange>()

        // 1. Extract Sheba (IR + 24 digits) first so its digits aren't misread as Card or National ID
        for (match in shebaRegex.findAll(normalized)) {
            val rawToken = match.value
            val afterPrefix = rawToken.dropWhile { !it.isLetterOrDigit() }.drop(2)
            val fixedDigits = FinancialValidators.fixNumericOcrConfusions(afterPrefix).filter { it.isDigit() }
            if (fixedDigits.length == 24) {
                val candidate = "IR$fixedDigits"
                if (FinancialValidators.isValidSheba(candidate)) {
                    consumedRanges.add(match.range)
                    results.putIfAbsent(
                        candidate,
                        DetectedItem(
                            type = ItemType.SHEBA,
                            normalizedValue = candidate,
                            formattedValue = FinancialValidators.formatSheba(candidate),
                            bank = IranianBankRegistry.findBySheba(candidate),
                            checksumLabel = "ISO 13616 Mod-97"
                        )
                    )
                }
            }
        }

        // 1b. Reversed RTL OCR Sheba (24 digits followed by IR)
        for (match in reversedShebaRegex.findAll(normalized)) {
            if (overlapsAny(match.range, consumedRanges)) continue
            val digits = match.value.filter { it.isDigit() }
            if (digits.length == 24) {
                val candidate = "IR$digits"
                if (FinancialValidators.isValidSheba(candidate)) {
                    consumedRanges.add(match.range)
                    results.putIfAbsent(
                        candidate,
                        DetectedItem(
                            type = ItemType.SHEBA,
                            normalizedValue = candidate,
                            formattedValue = FinancialValidators.formatSheba(candidate),
                            bank = IranianBankRegistry.findBySheba(candidate),
                            checksumLabel = "ISO 13616 Mod-97"
                        )
                    )
                }
            }
        }

        // 1c. Bare 24-digit Sheba next to "شبا" / "IBAN" keyword
        for (match in bareShebaContextRegex.findAll(normalized)) {
            if (overlapsAny(match.range, consumedRanges)) continue
            val digits = match.groupValues[1].filter { it.isDigit() }
            if (digits.length == 24) {
                val candidate = "IR$digits"
                if (FinancialValidators.isValidSheba(candidate)) {
                    consumedRanges.add(match.range)
                    results.putIfAbsent(
                        candidate,
                        DetectedItem(
                            type = ItemType.SHEBA,
                            normalizedValue = candidate,
                            formattedValue = FinancialValidators.formatSheba(candidate),
                            bank = IranianBankRegistry.findBySheba(candidate),
                            checksumLabel = "ISO 13616 Mod-97"
                        )
                    )
                }
            }
        }

        // 2. Extract 16-digit Iranian Bank Cards (including RTL-reversed 4-group recovery)
        val detectedExpiry = findExpiryDate(normalized)

        for (match in cardFourGroupsRegex.findAll(normalized)) {
            if (overlapsAny(match.range, consumedRanges)) continue
            val g1 = match.groupValues[1]
            val g2 = match.groupValues[2]
            val g3 = match.groupValues[3]
            val g4 = match.groupValues[4]

            val forward = g1 + g2 + g3 + g4
            val reversedGroups = g4 + g3 + g2 + g1

            val chosen = when {
                FinancialValidators.isValidCardNumber(forward) -> forward
                FinancialValidators.isValidCardNumber(reversedGroups) &&
                    IranianBankRegistry.findByCardNumber(reversedGroups) != null -> reversedGroups
                else -> null
            }

            if (chosen != null) {
                consumedRanges.add(match.range)
                val bank = IranianBankRegistry.findByCardNumber(chosen)
                results.putIfAbsent(
                    chosen,
                    DetectedItem(
                        type = ItemType.CARD,
                        normalizedValue = chosen,
                        formattedValue = FinancialValidators.formatCardNumber(chosen),
                        bank = bank,
                        expiryDate = detectedExpiry,
                        checksumLabel = "Luhn Mod-10",
                        confidenceHigh = bank != null
                    )
                )
            }
        }

        for (match in cardRegex.findAll(normalized)) {
            if (overlapsAny(match.range, consumedRanges)) continue
            val digits = match.value.filter { it.isDigit() }
            if (digits.length == 16 && FinancialValidators.isValidCardNumber(digits)) {
                consumedRanges.add(match.range)
                val bank = IranianBankRegistry.findByCardNumber(digits)
                results.putIfAbsent(
                    digits,
                    DetectedItem(
                        type = ItemType.CARD,
                        normalizedValue = digits,
                        formattedValue = FinancialValidators.formatCardNumber(digits),
                        bank = bank,
                        expiryDate = detectedExpiry,
                        checksumLabel = "Luhn Mod-10",
                        confidenceHigh = bank != null
                    )
                )
            }
        }

        // 3. Extract 10-digit Iranian National IDs (excluding Sheba/Card spans and mobile numbers)
        val lowerNormalized = normalized.lowercase()
        for (match in nationalIdRegex.findAll(normalized)) {
            if (overlapsAny(match.range, consumedRanges)) continue
            val digits = match.value.filter { it.isDigit() }
            if (digits.length != 10) continue

            val contextStart = (match.range.first - 40).coerceAtLeast(0)
            val contextEnd = (match.range.last + 25).coerceAtMost(lowerNormalized.length)
            val contextWindow = lowerNormalized.substring(contextStart, contextEnd)
            val hasKeywordContext = nationalIdKeywords.any { contextWindow.contains(it) }

            // Skip Iranian mobile numbers (09xxxxxxxx — 11 digits usually, or 9xxxxxxxxx — 10 digits)
            // unless explicitly labeled as National ID
            if (!hasKeywordContext && (digits.startsWith("09") || digits.startsWith("91") || digits.startsWith("93") || digits.startsWith("90") || digits.startsWith("99"))) {
                // Note: 09x mobile numbers are 11 digits, 91x/93x without leading 0 are 10 digits
                if (digits.startsWith("91") || digits.startsWith("93") || digits.startsWith("90") || digits.startsWith("99")) {
                    continue
                }
            }

            if (FinancialValidators.isValidNationalId(digits)) {
                consumedRanges.add(match.range)
                results.putIfAbsent(
                    digits,
                    DetectedItem(
                        type = ItemType.NATIONAL_ID,
                        normalizedValue = digits,
                        formattedValue = FinancialValidators.formatNationalId(digits),
                        bank = null,
                        checksumLabel = "Code Melli Mod-11",
                        confidenceHigh = hasKeywordContext || digits.startsWith("0")
                    )
                )
            }
        }

        return results.values.toList()
    }

    private fun findExpiryDate(normalizedText: String): String? {
        val match = expiryRegex.find(normalizedText) ?: return null
        return match.groupValues[1].replace(" ", "")
    }

    private fun overlapsAny(range: IntRange, existing: List<IntRange>): Boolean {
        return existing.any { other -> range.first <= other.last && other.first <= range.last }
    }
}
