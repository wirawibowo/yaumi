package com.yaumi.app.azan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IndonesiaLocationMapperTest {

    private val mapper = IndonesiaLocationMapper()

    private val provinces = listOf(
        "DKI Jakarta",
        "Jawa Barat",
        "DI Yogyakarta",
        "Kepulauan Bangka Belitung",
        "Nusa Tenggara Barat",
        "Papua",
        "Papua Barat"
    )

    @Test
    fun matchProvince_resolvesGeocoderAliases() {
        assertEquals("DKI Jakarta", mapper.matchProvince("Daerah Khusus Ibukota Jakarta", provinces))
        assertEquals("DI Yogyakarta", mapper.matchProvince("Jogja", provinces))
        assertEquals("Nusa Tenggara Barat", mapper.matchProvince("NTB", provinces))
    }

    @Test
    fun matchProvince_matchesExactNameIgnoringCaseAndPunctuation() {
        assertEquals("Jawa Barat", mapper.matchProvince("jawa barat", provinces))
        assertEquals("DI Yogyakarta", mapper.matchProvince("D.I. Yogyakarta", provinces))
    }

    @Test
    fun matchProvince_prefersExactMatchOverContainment() {
        // "Papua" is a prefix of "Papua Barat"; the exact entry must win.
        assertEquals("Papua", mapper.matchProvince("Papua", provinces))
    }

    @Test
    fun matchProvince_fallsBackToContainmentMatch() {
        assertEquals(
            "Kepulauan Bangka Belitung",
            mapper.matchProvince("Bangka Belitung", provinces)
        )
    }

    @Test
    fun matchProvince_returnsNullWhenNothingMatches() {
        assertNull(mapper.matchProvince("California", provinces))
    }

    @Test
    fun matchKabkota_ignoresKotaAndKabupatenPrefixes() {
        val kabkotaList = listOf("Kota Bandung", "Kabupaten Bogor", "Kota Surabaya")

        assertEquals("Kota Bandung", mapper.matchKabkota("Bandung", kabkotaList))
        assertEquals("Kabupaten Bogor", mapper.matchKabkota("Kab. Bogor", kabkotaList))
        assertEquals("Kota Surabaya", mapper.matchKabkota("kota surabaya", kabkotaList))
    }

    @Test
    fun matchKabkota_returnsNullWhenCityUnknown() {
        assertNull(mapper.matchKabkota("Springfield", listOf("Kota Bandung")))
    }
}
