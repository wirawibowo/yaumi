package com.yaumi.app.quran.data.model

data class SurahAsset(
    val id: Int,
    val name: String,
    val nameAr: String,
    val translation: String,
    val location: String,
    val numAyah: Int,
    val ayat: List<AyahAsset>
)

data class AyahAsset(
    val ayah: Int,
    val ar: String,
    val translationId: String
)
