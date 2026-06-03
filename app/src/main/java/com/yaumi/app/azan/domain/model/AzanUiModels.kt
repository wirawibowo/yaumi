package com.yaumi.app.azan.domain.model

data class PrayerTiming(
    val name: String,
    val time: String
)

data class AzanUiData(
    val cityName: String,
    val timezone: String,
    val dateReadable: String,
    val hijriReadable: String,
    val timings: List<PrayerTiming>,
    val provinceName: String = "",
    val isFallback: Boolean = false,
    val fallbackReason: String? = null
)
