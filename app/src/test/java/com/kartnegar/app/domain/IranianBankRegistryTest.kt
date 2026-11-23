package com.kartnegar.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IranianBankRegistryTest {

    @Test
    fun findByCardNumber_prefers8DigitBluBinOver6DigitSaman() {
        val blu = IranianBankRegistry.findByCardNumber("6219861912345674")
        val saman = IranianBankRegistry.findByCardNumber("6219861012345673")

        assertEquals("BLU", blu?.code)
        assertEquals("SAMAN", saman?.code)
    }

    @Test
    fun findByCardNumber_recognizesMajorBanksAndPersianDigits() {
        assertEquals("MELLI", IranianBankRegistry.findByCardNumber("۶۰۳۷۹۹۷۵۱۲۳۴۵۶۷۰")?.code)
        assertEquals("MELLAT", IranianBankRegistry.findByCardNumber("6104337812345678")?.code)
        assertEquals("PASARGAD", IranianBankRegistry.findByCardNumber("5022291012345678")?.code)
        assertNull(IranianBankRegistry.findByCardNumber("1234567890123456"))
    }

    @Test
    fun findBySheba_recognizesBankCode() {
        assertEquals("PARSIAN", IranianBankRegistry.findBySheba("IR820540102680020817909002")?.code)
        assertEquals("MELLAT", IranianBankRegistry.findBySheba("IR840120000000000123456789")?.code)
        assertNull(IranianBankRegistry.findBySheba("IR829990102680020817909002"))
    }
}
