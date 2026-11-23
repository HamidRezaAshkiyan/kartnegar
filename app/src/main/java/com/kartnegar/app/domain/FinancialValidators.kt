package com.kartnegar.app.domain

import java.math.BigInteger

enum class ItemType {
    CARD,
    SHEBA,
    NATIONAL_ID
}

data class ManualValidationResult(
    val type: ItemType?,
    val normalizedValue: String,
    val formattedValue: String,
    val isValid: Boolean,
    val bank: IranianBank?,
    val message: String
)

object FinancialValidators {

    private val mod97 = BigInteger.valueOf(97)

    fun normalizeDigits(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> sb.append(('0'.code + (ch.code - '۰'.code)).toChar())
                in '٠'..'٩' -> sb.append(('0'.code + (ch.code - '٠'.code)).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Fixes common OCR letter-to-digit confusions strictly inside numeric candidate tokens.
     */
    fun fixNumericOcrConfusions(token: String): String {
        val sb = StringBuilder(token.length)
        for (ch in normalizeDigits(token)) {
            when (ch) {
                'O', 'o', 'Q', 'D' -> sb.append('0')
                'I', 'l', '|', '!' -> sb.append('1')
                'S', 's' -> sb.append('5')
                'B' -> sb.append('8')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Standard Luhn (Mod-10) check for 16-digit Iranian bank cards.
     */
    fun isValidCardNumber(raw: String): Boolean {
        val digits = normalizeDigits(raw).filter { it.isDigit() }
        if (digits.length != 16) return false
        if (digits.all { it == digits[0] }) return false

        var sum = 0
        for (i in 0 until 16) {
            var d = digits[i] - '0'
            if (i % 2 == 0) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
        }
        return sum % 10 == 0
    }

    /**
     * ISO 13616 Mod-97 check for Iranian Sheba (IR + 24 digits = 26 chars).
     */
    fun isValidSheba(raw: String): Boolean {
        val clean = normalizeDigits(raw).uppercase().filter { it.isLetterOrDigit() }
        if (clean.length != 26 || !clean.startsWith("IR")) return false
        val body = clean.substring(2)
        if (!body.all { it.isDigit() }) return false

        // Move "IR" + check digits (first 4 chars) to end; I=18, R=27 -> "1827"
        val rearranged = clean.substring(4) + "1827" + clean.substring(2, 4)
        return try {
            BigInteger(rearranged).mod(mod97) == BigInteger.ONE
        } catch (_: NumberFormatException) {
            false
        }
    }

    /**
     * Official Iranian National ID (Code Melli) Mod-11 check (10 digits).
     */
    fun isValidNationalId(raw: String): Boolean {
        val digits = normalizeDigits(raw).filter { it.isDigit() }
        if (digits.length != 10) return false
        // Reject all-identical digits (e.g. 0000000000, 1111111111)
        if (digits.all { it == digits[0] }) return false

        var sum = 0
        for (i in 0 until 9) {
            sum += (digits[i] - '0') * (10 - i)
        }
        val remainder = sum % 11
        val checkDigit = digits[9] - '0'
        return if (remainder < 2) {
            checkDigit == remainder
        } else {
            checkDigit == 11 - remainder
        }
    }

    fun formatCardNumber(raw: String): String {
        val digits = normalizeDigits(raw).filter { it.isDigit() }
        if (digits.length != 16) return digits
        return digits.chunked(4).joinToString(" ")
    }

    fun formatSheba(raw: String): String {
        val clean = normalizeDigits(raw).uppercase().filter { it.isLetterOrDigit() }
        if (clean.length != 26) return clean
        return buildString {
            append(clean.substring(0, 4))
            append(' ')
            append(clean.substring(4, 8))
            append(' ')
            append(clean.substring(8, 12))
            append(' ')
            append(clean.substring(12, 16))
            append(' ')
            append(clean.substring(16, 20))
            append(' ')
            append(clean.substring(20, 24))
            append(' ')
            append(clean.substring(24, 26))
        }
    }

    fun formatNationalId(raw: String): String {
        val digits = normalizeDigits(raw).filter { it.isDigit() }
        if (digits.length != 10) return digits
        return "${digits.substring(0, 3)}-${digits.substring(3, 9)}-${digits.substring(9, 10)}"
    }

    fun formatByType(type: ItemType, normalizedValue: String): String = when (type) {
        ItemType.CARD -> formatCardNumber(normalizedValue)
        ItemType.SHEBA -> formatSheba(normalizedValue)
        ItemType.NATIONAL_ID -> formatNationalId(normalizedValue)
    }

    fun validateManualInput(input: String): ManualValidationResult {
        val clean = normalizeDigits(input).uppercase().filter { it.isLetterOrDigit() }
        if (clean.isEmpty()) {
            return ManualValidationResult(
                type = null,
                normalizedValue = "",
                formattedValue = "",
                isValid = false,
                bank = null,
                message = "Enter a 16-digit Card, 26-char Sheba (IR...), or 10-digit National ID"
            )
        }

        // Sheba check (with or without IR prefix)
        if (clean.startsWith("IR") || clean.length == 24) {
            val candidate = if (clean.startsWith("IR")) clean else "IR$clean"
            val valid = isValidSheba(candidate)
            val bank = IranianBankRegistry.findBySheba(candidate)
            return ManualValidationResult(
                type = ItemType.SHEBA,
                normalizedValue = candidate,
                formattedValue = formatSheba(candidate),
                isValid = valid,
                bank = bank,
                message = if (valid) {
                    "Valid Sheba (ISO 13616 Mod-97)"
                } else {
                    "Invalid Sheba checksum or length (needs IR + 24 digits)"
                }
            )
        }

        val digitsOnly = clean.filter { it.isDigit() }
        return when (digitsOnly.length) {
            16 -> {
                val valid = isValidCardNumber(digitsOnly)
                val bank = IranianBankRegistry.findByCardNumber(digitsOnly)
                ManualValidationResult(
                    type = ItemType.CARD,
                    normalizedValue = digitsOnly,
                    formattedValue = formatCardNumber(digitsOnly),
                    isValid = valid,
                    bank = bank,
                    message = if (valid) {
                        "Valid Card Number (Luhn Mod-10)"
                    } else {
                        "Invalid Card Number (Luhn checksum failed)"
                    }
                )
            }
            10 -> {
                val valid = isValidNationalId(digitsOnly)
                ManualValidationResult(
                    type = ItemType.NATIONAL_ID,
                    normalizedValue = digitsOnly,
                    formattedValue = formatNationalId(digitsOnly),
                    isValid = valid,
                    bank = null,
                    message = if (valid) {
                        "Valid Iranian National ID (Mod-11)"
                    } else {
                        "Invalid National ID (Mod-11 checksum failed)"
                    }
                )
            }
            else -> ManualValidationResult(
                type = null,
                normalizedValue = clean,
                formattedValue = clean,
                isValid = false,
                bank = null,
                message = "Detected ${digitsOnly.length} digits — expected 10 (National ID), 16 (Card), or IR + 24 (Sheba)"
            )
        }
    }
}
