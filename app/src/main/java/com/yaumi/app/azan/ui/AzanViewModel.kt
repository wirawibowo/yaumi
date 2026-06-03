package com.yaumi.app.azan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.azan.data.AzanRepository
import com.yaumi.app.azan.data.AzanScheduleRules
import com.yaumi.app.azan.data.AzanSettings
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.data.AzanTimeUtils
import com.yaumi.app.azan.domain.model.AzanUiData
import com.yaumi.app.azan.notifications.AzanNotificationHelper
import com.yaumi.app.azan.alarm.AzanAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AzanUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: AzanUiData? = null,
    val settings: AzanSettings = AzanSettings()
)

class AzanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AzanRepository(application.applicationContext)
    private val settingsStore = AzanSettingsStore(application.applicationContext)
    private val _uiState = MutableStateFlow(AzanUiState())
    val uiState: StateFlow<AzanUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(settings = settingsStore.load()) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentSettings = _uiState.value.settings
            runCatching { repository.loadAzanData() }
                .onSuccess { data ->
                    val withOffsets = AzanTimeUtils.applyOffsets(data, currentSettings.manualOffsets)
                    _uiState.update { it.copy(isLoading = false, data = withOffsets) }
                    scheduleIfEnabled(withOffsets, currentSettings)
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Gagal memuat jadwal azan")
                    }
                }
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(notificationEnabled = enabled)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
        _uiState.value.data?.let { scheduleIfEnabled(it, updated) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(reminderEnabled = enabled)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
        _uiState.value.data?.let { scheduleIfEnabled(it, updated) }
    }

    fun setReminderMinutes(minutes: Int) {
        val safeValue = minutes.coerceIn(1, 60)
        val updated = _uiState.value.settings.copy(reminderMinutes = safeValue)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
        _uiState.value.data?.let { scheduleIfEnabled(it, updated) }
    }

    fun setAudioEnabled(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(audioEnabled = enabled)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun setSelectedAudio(fileName: String) {
        val updated = _uiState.value.settings.copy(selectedAudio = fileName)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun setSelectedFajrAudio(fileName: String) {
        val updated = _uiState.value.settings.copy(selectedFajrAudio = fileName)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun setPrayerOffset(prayer: String, minutes: Int) {
        val clamped = minutes.coerceIn(-30, 30)
        val updatedOffsets = _uiState.value.settings.manualOffsets.toMutableMap().apply {
            this[prayer] = clamped
        }
        val updated = _uiState.value.settings.copy(manualOffsets = updatedOffsets)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
        refresh()
    }

    fun setPrayerEnabled(prayer: String, enabled: Boolean) {
        val updatedEnabled = _uiState.value.settings.sholatEnabled.toMutableMap().apply {
            this[prayer] = enabled
        }
        val updated = _uiState.value.settings.copy(sholatEnabled = updatedEnabled)
        settingsStore.save(updated)
        _uiState.update { it.copy(settings = updated) }
        _uiState.value.data?.let { scheduleIfEnabled(it, updated) }
    }

    private fun scheduleIfEnabled(data: AzanUiData, settings: AzanSettings) {
        val appContext = getApplication<Application>().applicationContext
        AzanNotificationHelper.ensureChannel(appContext)
        if (!settings.notificationEnabled) return

        val target = AzanScheduleRules.selectSchedulableTimings(
            timings = data.timings,
            sholatEnabled = settings.sholatEnabled
        )

        AzanAlarmScheduler.scheduleForToday(
            context = appContext,
            timings = target,
            reminderEnabled = settings.reminderEnabled,
            reminderMinutes = settings.reminderMinutes
        )
    }
}
