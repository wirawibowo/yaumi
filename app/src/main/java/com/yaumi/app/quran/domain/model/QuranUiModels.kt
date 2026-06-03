package com.yaumi.app.quran.domain.model

data class SurahSummary(
    val id: Int,
    val latinName: String,
    val arabicName: String,
    val translation: String,
    val location: String,
    val ayahCount: Int
)

data class AyahLine(
    val number: Int,
    val arabicText: String,
    val translationId: String,
    val tafsirText: String? = null
)
