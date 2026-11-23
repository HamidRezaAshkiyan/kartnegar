package com.kartnegar.app.domain

data class IranianBank(
    val code: String,
    val nameFa: String,
    val nameEn: String,
    val accentColorHex: Long,
    val cardBins: List<String>,
    val shebaCodes: List<String>
)

object IranianBankRegistry {

    val banks: List<IranianBank> = listOf(
        // Special 8-digit BINs (checked before 6-digit parent BINs)
        IranianBank(
            code = "BLU",
            nameFa = "بلوبانک (سامان)",
            nameEn = "Blu Bank",
            accentColorHex = 0xFF0284C7,
            cardBins = listOf("62198619", "62198618"),
            shebaCodes = emptyList()
        ),
        IranianBank(
            code = "MELLI",
            nameFa = "بانک ملی ایران",
            nameEn = "Bank Melli Iran",
            accentColorHex = 0xFFB91C1C,
            cardBins = listOf("603799", "170019", "6037991", "6037997"),
            shebaCodes = listOf("017")
        ),
        IranianBank(
            code = "MELLAT",
            nameFa = "بانک ملت",
            nameEn = "Bank Mellat",
            accentColorHex = 0xFFDC2626,
            cardBins = listOf("610433", "991975"),
            shebaCodes = listOf("012")
        ),
        IranianBank(
            code = "SADERAT",
            nameFa = "بانک صادرات ایران",
            nameEn = "Bank Saderat",
            accentColorHex = 0xFF1D4ED8,
            cardBins = listOf("603769", "903769"),
            shebaCodes = listOf("019")
        ),
        IranianBank(
            code = "TEJARAT",
            nameFa = "بانک تجارت",
            nameEn = "Bank Tejarat",
            accentColorHex = 0xFF2563EB,
            cardBins = listOf("627353", "585983"),
            shebaCodes = listOf("018")
        ),
        IranianBank(
            code = "SEPAH",
            nameFa = "بانک سپه",
            nameEn = "Bank Sepah",
            accentColorHex = 0xFFD97706,
            cardBins = listOf("589210", "639370", "639599", "627381", "505801", "636949"),
            shebaCodes = listOf("015", "052", "063", "065", "073")
        ),
        IranianBank(
            code = "KESHAVARZI",
            nameFa = "بانک کشاورزی",
            nameEn = "Bank Keshavarzi",
            accentColorHex = 0xFF15803D,
            cardBins = listOf("603770", "639217"),
            shebaCodes = listOf("016")
        ),
        IranianBank(
            code = "MASKAN",
            nameFa = "بانک مسکن",
            nameEn = "Bank Maskan",
            accentColorHex = 0xFFEA580C,
            cardBins = listOf("628023"),
            shebaCodes = listOf("014")
        ),
        IranianBank(
            code = "SAMAN",
            nameFa = "بانک سامان",
            nameEn = "Saman Bank",
            accentColorHex = 0xFF0EA5E9,
            cardBins = listOf("621986"),
            shebaCodes = listOf("056")
        ),
        IranianBank(
            code = "PASARGAD",
            nameFa = "بانک پاسارگاد",
            nameEn = "Bank Pasargad",
            accentColorHex = 0xFFCA8A04,
            cardBins = listOf("502229", "639347"),
            shebaCodes = listOf("057")
        ),
        IranianBank(
            code = "PARSIAN",
            nameFa = "بانک پارسیان",
            nameEn = "Parsian Bank",
            accentColorHex = 0xFF9A3412,
            cardBins = listOf("622106", "639194", "627884"),
            shebaCodes = listOf("054")
        ),
        IranianBank(
            code = "EN",
            nameFa = "بانک اقتصاد نوین",
            nameEn = "EN Bank",
            accentColorHex = 0xFF7C3AED,
            cardBins = listOf("627412"),
            shebaCodes = listOf("055")
        ),
        IranianBank(
            code = "SHAHR",
            nameFa = "بانک شهر",
            nameEn = "Shahr Bank",
            accentColorHex = 0xFFE11D48,
            cardBins = listOf("502806", "504706"),
            shebaCodes = listOf("061")
        ),
        IranianBank(
            code = "AYANDEH",
            nameFa = "بانک آینده",
            nameEn = "Ayandeh Bank",
            accentColorHex = 0xFF78350F,
            cardBins = listOf("636214"),
            shebaCodes = listOf("062")
        ),
        IranianBank(
            code = "DEY",
            nameFa = "بانک دی",
            nameEn = "Bank Day",
            accentColorHex = 0xFF0D9488,
            cardBins = listOf("502938"),
            shebaCodes = listOf("066")
        ),
        IranianBank(
            code = "SINA",
            nameFa = "بانک سینا",
            nameEn = "Sina Bank",
            accentColorHex = 0xFF1E40AF,
            cardBins = listOf("639346"),
            shebaCodes = listOf("059")
        ),
        IranianBank(
            code = "KARAFARIN",
            nameFa = "بانک کارآفرین",
            nameEn = "Karafarin Bank",
            accentColorHex = 0xFF047857,
            cardBins = listOf("627488", "502910"),
            shebaCodes = listOf("053")
        ),
        IranianBank(
            code = "SARMAYEH",
            nameFa = "بانک سرمایه",
            nameEn = "Sarmayeh Bank",
            accentColorHex = 0xFF4F46E5,
            cardBins = listOf("639607"),
            shebaCodes = listOf("058")
        ),
        IranianBank(
            code = "REFAH",
            nameFa = "بانک رفاه کارگران",
            nameEn = "Refah Bank",
            accentColorHex = 0xFF0369A1,
            cardBins = listOf("589463"),
            shebaCodes = listOf("013")
        ),
        IranianBank(
            code = "POST",
            nameFa = "پست بانک ایران",
            nameEn = "Post Bank",
            accentColorHex = 0xFF16A34A,
            cardBins = listOf("627760"),
            shebaCodes = listOf("021")
        ),
        IranianBank(
            code = "SANAT",
            nameFa = "بانک صنعت و معدن",
            nameEn = "Bank of Industry & Mine",
            accentColorHex = 0xFF475569,
            cardBins = listOf("627961"),
            shebaCodes = listOf("011")
        ),
        IranianBank(
            code = "TOSEE_SADERAT",
            nameFa = "بانک توسعه صادرات",
            nameEn = "Export Development Bank",
            accentColorHex = 0xFF0F766E,
            cardBins = listOf("627648", "207177"),
            shebaCodes = listOf("020")
        ),
        IranianBank(
            code = "TOSEE_TAAVON",
            nameFa = "بانک توسعه تعاون",
            nameEn = "Tose'e Taavon Bank",
            accentColorHex = 0xFF059669,
            cardBins = listOf("502908"),
            shebaCodes = listOf("022")
        ),
        IranianBank(
            code = "MEHR_IRAN",
            nameFa = "بانک قرض‌الحسنه مهر ایران",
            nameEn = "Mehr Iran Bank",
            accentColorHex = 0xFF059669,
            cardBins = listOf("606373"),
            shebaCodes = listOf("060")
        ),
        IranianBank(
            code = "RESALAT",
            nameFa = "بانک قرض‌الحسنه رسالت",
            nameEn = "Resalat Bank",
            accentColorHex = 0xFF0284C7,
            cardBins = listOf("504172"),
            shebaCodes = listOf("070")
        ),
        IranianBank(
            code = "GARDESHGARI",
            nameFa = "بانک گردشگری",
            nameEn = "Tourism Bank",
            accentColorHex = 0xFFDC2626,
            cardBins = listOf("505416"),
            shebaCodes = listOf("064")
        ),
        IranianBank(
            code = "IRAN_ZAMIN",
            nameFa = "بانک ایران زمین",
            nameEn = "Iran Zamin Bank",
            accentColorHex = 0xFF7E22CE,
            cardBins = listOf("505785"),
            shebaCodes = listOf("069")
        ),
        IranianBank(
            code = "KHAVARMIANEH",
            nameFa = "بانک خاورمیانه",
            nameEn = "Middle East Bank",
            accentColorHex = 0xFFB45309,
            cardBins = listOf("585947"),
            shebaCodes = listOf("078")
        ),
        IranianBank(
            code = "MELAL",
            nameFa = "موسسه اعتباری ملل",
            nameEn = "Melal Credit Institution",
            accentColorHex = 0xFF1D4ED8,
            cardBins = listOf("606256"),
            shebaCodes = listOf("075")
        ),
        IranianBank(
            code = "NOOR",
            nameFa = "موسسه اعتباری نور",
            nameEn = "Noor Credit Institution",
            accentColorHex = 0xFF0891B2,
            cardBins = listOf("507677"),
            shebaCodes = listOf("080")
        ),
        IranianBank(
            code = "MARKAZI",
            nameFa = "بانک مرکزی",
            nameEn = "Central Bank of Iran",
            accentColorHex = 0xFF334155,
            cardBins = listOf("636795"),
            shebaCodes = listOf("010")
        )
    )

    private val sortedBinPairs: List<Pair<String, IranianBank>> = banks
        .flatMap { bank -> bank.cardBins.map { bin -> bin to bank } }
        .sortedByDescending { it.first.length }

    private val shebaCodeMap: Map<String, IranianBank> = banks
        .flatMap { bank -> bank.shebaCodes.map { code -> code to bank } }
        .toMap()

    fun findByCardNumber(cardDigits: String): IranianBank? {
        val clean = FinancialValidators.normalizeDigits(cardDigits).filter { it.isDigit() }
        if (clean.length < 6) return null
        return sortedBinPairs.firstOrNull { (bin, _) -> clean.startsWith(bin) }?.second
    }

    fun findBySheba(sheba: String): IranianBank? {
        val clean = FinancialValidators.normalizeDigits(sheba)
            .uppercase()
            .filter { it.isLetterOrDigit() }
        if (clean.length < 7 || !clean.startsWith("IR")) return null
        // Format: IR + 2 check digits + 3 bank code digits + 19 account digits
        val bankCode = clean.substring(4, 7)
        return shebaCodeMap[bankCode]
    }
}
