package com.kartnegar.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialValidatorsTest {

    @Test
    fun normalizeDigits_convertsPersianAndArabicNumerals() {
        assertEquals("0123456789", FinancialValidators.normalizeDigits("۰۱۲۳۴۵۶۷۸۹"))
        assertEquals("0123456789", FinancialValidators.normalizeDigits("٠١٢٣٤٥٦٧٨٩"))
        assertEquals("IR8205401026", FinancialValidators.normalizeDigits("IR۸۲۰۵۴۰۱۰۲۶"))
    }

    @Test
    fun isValidCardNumber_validatesLuhnCorrectly() {
        assertTrue(FinancialValidators.isValidCardNumber("6037997512345670"))
        assertTrue(FinancialValidators.isValidCardNumber("6037-9975-1234-5670"))
        assertTrue(FinancialValidators.isValidCardNumber("۶۰۳۷ ۹۹۷۵ ۱۲۳۴ ۵۶۷۰"))
        assertFalse(FinancialValidators.isValidCardNumber("6037997512345671")) // wrong check digit
        assertFalse(FinancialValidators.isValidCardNumber("0000000000000000")) // all same
        assertFalse(FinancialValidators.isValidCardNumber("603799751234"))     // too short
    }

    @Test
    fun isValidSheba_validatesIso13616Mod97() {
        assertTrue(FinancialValidators.isValidSheba("IR820540102680020817909002"))
        assertTrue(FinancialValidators.isValidSheba("ir84 0120 0000 0000 0123 4567 89"))
        assertTrue(FinancialValidators.isValidSheba("IR۸۲۰۵۴۰۱۰۲۶۸۰۰۲۰۸۱۷۹۰۹۰۰۲"))
        assertFalse(FinancialValidators.isValidSheba("IR830540102680020817909002")) // bad check digits
        assertFalse(FinancialValidators.isValidSheba("DE820540102680020817909002")) // non-IR
        assertFalse(FinancialValidators.isValidSheba("IR8205401026"))              // too short
    }

    @Test
    fun isValidNationalId_validatesCodeMelliMod11() {
        assertTrue(FinancialValidators.isValidNationalId("0012345679"))
        assertTrue(FinancialValidators.isValidNationalId("049-123456-2"))
        assertTrue(FinancialValidators.isValidNationalId("۰۰۱۲۳۴۵۶۷۹"))
        assertFalse(FinancialValidators.isValidNationalId("0012345678")) // wrong check digit
        assertFalse(FinancialValidators.isValidNationalId("1111111111")) // all identical
        assertFalse(FinancialValidators.isValidNationalId("0000000000")) // all identical
    }

    @Test
    fun formatters_produceCleanReadableOutput() {
        assertEquals("6037 9975 1234 5670", FinancialValidators.formatCardNumber("6037997512345670"))
        assertEquals(
            "IR82 0540 1026 8002 0817 9090 02",
            FinancialValidators.formatSheba("IR820540102680020817909002")
        )
        assertEquals("001-234567-9", FinancialValidators.formatNationalId("0012345679"))
    }

    @Test
    fun validateManualInput_autoDetectsTypeAndBank() {
        val cardRes = FinancialValidators.validateManualInput("6219-8619-1234-5674")
        assertTrue(cardRes.isValid)
        assertEquals(ItemType.CARD, cardRes.type)
        assertEquals("BLU", cardRes.bank?.code)

        val shebaRes = FinancialValidators.validateManualInput("840120000000000123456789")
        assertTrue(shebaRes.isValid)
        assertEquals(ItemType.SHEBA, shebaRes.type)
        assertEquals("MELLAT", shebaRes.bank?.code)

        val nidRes = FinancialValidators.validateManualInput("0491234562")
        assertTrue(nidRes.isValid)
        assertEquals(ItemType.NATIONAL_ID, nidRes.type)
    }
}
