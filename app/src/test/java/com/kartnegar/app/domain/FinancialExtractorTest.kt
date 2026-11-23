package com.kartnegar.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialExtractorTest {

    @Test
    fun extract_parsesMixedPersianBankingScreenshot() {
        val ocrText = """
            بانک ملی ایران
            شماره کارت: ۶۰۳۷ ۹۹۷۵ ۱۲۳۴ ۵۶۷۰
            تاریخ انقضا: 06/11
            شماره شبا: IR84 0120 0000 0000 0123 4567 89
            کد ملی: ۰۰۱۲۳۴۵۶۷۹
        """.trimIndent()

        val items = FinancialExtractor.extract(ocrText)
        assertEquals(3, items.size)

        val card = items.first { it.type == ItemType.CARD }
        assertEquals("6037997512345670", card.normalizedValue)
        assertEquals("MELLI", card.bank?.code)
        assertEquals("06/11", card.expiryDate)

        val sheba = items.first { it.type == ItemType.SHEBA }
        assertEquals("IR840120000000000123456789", sheba.normalizedValue)
        assertEquals("MELLAT", sheba.bank?.code)

        val nid = items.first { it.type == ItemType.NATIONAL_ID }
        assertEquals("0012345679", nid.normalizedValue)
    }

    @Test
    fun extract_recoversRtlReversedFourGroupCardNumber() {
        // Common RTL OCR issue: 4-digit groups appear right-to-left
        val reversedOcr = "5670 1234 9975 6037"
        val items = FinancialExtractor.extract(reversedOcr)

        assertEquals(1, items.size)
        assertEquals(ItemType.CARD, items[0].type)
        assertEquals("6037997512345670", items[0].normalizedValue)
        assertEquals("MELLI", items[0].bank?.code)
    }

    @Test
    fun extract_fixesOcrPrefixConfusionInSheba() {
        val noisySheba = "1R82 054O 1026 8002 0817 9090 02"
        val items = FinancialExtractor.extract(noisySheba)

        assertEquals(1, items.size)
        assertEquals(ItemType.SHEBA, items[0].type)
        assertEquals("IR820540102680020817909002", items[0].normalizedValue)
        assertEquals("PARSIAN", items[0].bank?.code)
    }

    @Test
    fun extract_doesNotLeakShebaSubstringsAsCardsOrNationalIds() {
        val onlySheba = "IR820540102680020817909002"
        val items = FinancialExtractor.extract(onlySheba)

        assertEquals(1, items.size)
        assertEquals(ItemType.SHEBA, items[0].type)
    }

    @Test
    fun extractFromMultiplePasses_mergesAndDeduplicates() {
        val engPass = "6037 9975 1234 5670\nEXP 05/09"
        val fasPass = "۶۰۳۷۹۹۷۵۱۲۳۴۵۶۷۰\nکد ملی ۰۴۹۱۲۳۴۵۶۲"

        val items = FinancialExtractor.extractFromMultiplePasses(listOf(engPass, fasPass))
        assertEquals(2, items.size)
        assertTrue(items.any { it.type == ItemType.CARD && it.normalizedValue == "6037997512345670" && it.expiryDate == "05/09" })
        assertTrue(items.any { it.type == ItemType.NATIONAL_ID && it.normalizedValue == "0491234562" })
    }
}
