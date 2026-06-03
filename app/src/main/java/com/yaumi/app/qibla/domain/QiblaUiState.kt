package com.yaumi.app.qibla.domain

data class QiblaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val locationName: String = "-",
    val distanceKm: Double = 0.0,
    val qiblaBearing: Double = 0.0,
    val heading: Double = 0.0,
    val turnDegrees: Double = 0.0,
    val sensorAccuracy: Int = -1
)
