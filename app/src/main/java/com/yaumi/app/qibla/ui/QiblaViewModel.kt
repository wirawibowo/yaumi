package com.yaumi.app.qibla.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.qibla.data.QiblaMath
import com.yaumi.app.qibla.data.QiblaRepository
import com.yaumi.app.qibla.domain.QiblaUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class QiblaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QiblaRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()
    private var smoothedHeading = 0.0
    private var hasSmoothedValue = false
    private var headingJob: Job? = null

    init {
        refresh()
        startHeadingUpdates()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getQiblaData() }
                .onSuccess { data ->
                    _uiState.update {
                        val turn = QiblaMath.turnDelta(it.heading, data.qiblaBearing)
                        it.copy(
                            isLoading = false,
                            locationName = data.locationName,
                            distanceKm = data.distanceKm,
                            qiblaBearing = data.qiblaBearing,
                            turnDegrees = turn
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Gagal memuat data qibla"
                        )
                    }
                }
        }
    }

    private fun startHeadingUpdates() {
        headingJob?.cancel()
        headingJob = viewModelScope.launch {
            repository.headingFlow().collect { reading ->
                val heading = reading.headingDegrees
                val deltaFromCurrent = normalizeDelta(heading - smoothedHeading)
                val alpha = when {
                    !hasSmoothedValue -> 1.0
                    abs(deltaFromCurrent) > 45.0 -> 0.5
                    abs(deltaFromCurrent) > 15.0 -> 0.3
                    else -> 0.18
                }
                smoothedHeading = if (!hasSmoothedValue) {
                    hasSmoothedValue = true
                    normalizeHeading(heading)
                } else {
                    normalizeHeading(smoothedHeading + (alpha * deltaFromCurrent))
                }

                _uiState.update {
                    it.copy(
                        heading = smoothedHeading,
                        turnDegrees = QiblaMath.turnDelta(smoothedHeading, it.qiblaBearing),
                        sensorAccuracy = reading.accuracy
                    )
                }
            }
        }
    }

    override fun onCleared() {
        headingJob?.cancel()
        super.onCleared()
    }

    private fun normalizeHeading(value: Double): Double {
        return ((value % 360.0) + 360.0) % 360.0
    }

    private fun normalizeDelta(value: Double): Double {
        return ((value + 540.0) % 360.0) - 180.0
    }
}
