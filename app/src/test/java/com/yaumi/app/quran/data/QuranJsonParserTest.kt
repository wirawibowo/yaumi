package com.yaumi.app.quran.data

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranJsonParserTest {

    private val sample = """
        [
          {
            "id": 1,
            "name": "Al-Fatihah",
            "name_ar": "الفاتحة",
            "translation": "Pembukaan",
            "location": "Makkiyah",
            "num_ayah": 2,
            "ayat": [
              {"ayah": 1, "ar": "بِسْمِ اللَّهِ", "id": "Dengan nama Allah"},
              {"ayah": 2, "ar": "الْحَمْدُ لِلَّهِ", "id": "Segala puji bagi Allah"}
            ]
          },
          {
            "id": 114,
            "name": "An-Nas",
            "name_ar": "الناس",
            "translation": "Manusia",
            "location": "Makkiyah",
            "num_ayah": 6,
            "ayat": []
          }
        ]
    """.trimIndent()

    @Test
    fun parseSurahList_mapsAllFields() {
        val surahList = QuranJsonParser.parseSurahList(sample)

        assertEquals(2, surahList.size)

        val fatihah = surahList[0]
        assertEquals(1, fatihah.id)
        assertEquals("Al-Fatihah", fatihah.name)
        assertEquals("الفاتحة", fatihah.nameAr)
        assertEquals("Pembukaan", fatihah.translation)
        assertEquals("Makkiyah", fatihah.location)
        assertEquals(2, fatihah.numAyah)
        assertEquals(2, fatihah.ayat.size)
        assertEquals(1, fatihah.ayat[0].ayah)
        assertEquals("بِسْمِ اللَّهِ", fatihah.ayat[0].ar)
        assertEquals("Dengan nama Allah", fatihah.ayat[0].translationId)

        assertEquals(114, surahList[1].id)
        assertTrue(surahList[1].ayat.isEmpty())
    }

    @Test
    fun parseSurahList_emptyArrayGivesEmptyList() {
        assertTrue(QuranJsonParser.parseSurahList("[]").isEmpty())
    }

    @Test(expected = JSONException::class)
    fun parseSurahList_missingAyatFieldThrows() {
        QuranJsonParser.parseSurahList(
            """[{"id": 1, "name": "X", "name_ar": "x", "translation": "t", "location": "l", "num_ayah": 1}]"""
        )
    }

    @Test(expected = JSONException::class)
    fun parseSurahList_nonArrayInputThrows() {
        QuranJsonParser.parseSurahList("{}")
    }
}
