package com.yaumi.app.tadabur.domain

data class TadaburDay(
    val dayIndex: Int,
    val surahName: String,
    val ayahRange: String,
    val arabicText: String,
    val translationId: String,
    val hikmah: String,
    val praktik: List<String>,
    val amalTracker: List<String>,
    val hadithArabic: String,
    val hadithTranslation: String,
    val hadithReference: String
)
