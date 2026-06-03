package com.yaumi.app.settings.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.azan.audio.AzanAudioPlayer
import com.yaumi.app.azan.data.AzanSettings
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.data.AzanRepository
import com.yaumi.app.tadabur.worker.TadaburAlarmScheduler
import com.yaumi.app.tadabur.data.TadaburSettings
import com.yaumi.app.tadabur.data.TadaburSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val azanSettings: AzanSettings = AzanSettings(),
    val isAzanAudioPlaying: Boolean = false,
    val tadaburSettings: TadaburSettings = TadaburSettings(),
    val availableProvinces: List<String> = emptyList(),
    val availableKabkota: List<String> = emptyList(),
    val locationLoading: Boolean = false,
    val locationError: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = AzanSettingsStore(application.applicationContext)
    private val repository = AzanRepository(application.applicationContext)
    private val tadaburStore = TadaburSettingsStore(application.applicationContext)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val settings = store.load()
        val tadaburSettings = tadaburStore.load()
        AzanAudioPlayer.setVolumePercent(settings.audioVolumePercent)
        _uiState.update {
            it.copy(
                azanSettings = settings,
                isAzanAudioPlaying = AzanAudioPlayer.isPlaying(),
                tadaburSettings = tadaburSettings
            )
        }
        loadProvinces()
        if (settings.selectedProvince.isNotBlank()) {
            loadKabkota(settings.selectedProvince)
        }
    }

    fun loadProvinces() {
        _uiState.update { it.copy(locationLoading = true, locationError = null) }
        viewModelScope.launch {
            runCatching { repository.fetchProvinces() }
                .onSuccess { provinces ->
                    _uiState.update {
                        it.copy(
                            availableProvinces = provinces,
                            locationLoading = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(locationLoading = false, locationError = err.message ?: "Gagal memuat provinsi")
                    }
                }
        }
    }

    fun loadKabkota(province: String) {
        _uiState.update { it.copy(locationLoading = true, locationError = null, availableKabkota = emptyList()) }
        viewModelScope.launch {
            runCatching { repository.fetchKabkota(province) }
                .onSuccess { kabkota ->
                    _uiState.update {
                        it.copy(
                            availableKabkota = kabkota,
                            locationLoading = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(locationLoading = false, locationError = err.message ?: "Gagal memuat kab/kota")
                    }
                }
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        persist(_uiState.value.azanSettings.copy(notificationEnabled = enabled))
    }

    fun setBackgroundServiceEnabled(enabled: Boolean) {
        persist(_uiState.value.azanSettings.copy(backgroundServiceEnabled = enabled))
    }

    fun setReminderEnabled(enabled: Boolean) {
        persist(_uiState.value.azanSettings.copy(reminderEnabled = enabled))
    }

    fun setAudioEnabled(enabled: Boolean) {
        persist(_uiState.value.azanSettings.copy(audioEnabled = enabled))
    }

    fun setAudioVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        AzanAudioPlayer.setVolumePercent(clamped)
        persist(_uiState.value.azanSettings.copy(audioVolumePercent = clamped))
    }

    fun stopAzanAudio() {
        AzanAudioPlayer.stopCurrent()
        _uiState.update { it.copy(isAzanAudioPlaying = false) }
    }

    fun setSelectedAudio(file: String) {
        persist(_uiState.value.azanSettings.copy(selectedAudio = file))
    }

    fun setSelectedFajrAudio(file: String) {
        persist(_uiState.value.azanSettings.copy(selectedFajrAudio = file))
    }

    fun setUseManualLocation(enabled: Boolean) {
        val updated = _uiState.value.azanSettings.copy(useManualLocation = enabled)
        persist(updated)
        if (enabled) {
            loadProvinces()
            if (updated.selectedProvince.isNotBlank()) {
                loadKabkota(updated.selectedProvince)
            }
        }
    }

    fun setSelectedProvince(province: String) {
        val updated = _uiState.value.azanSettings.copy(
            selectedProvince = province,
            selectedKabkota = ""
        )
        persist(updated)
        if (province.isNotBlank()) {
            loadKabkota(province)
        }
    }

    fun setSelectedKabkota(kabkota: String) {
        val updated = _uiState.value.azanSettings.copy(selectedKabkota = kabkota)
        persist(updated)
    }

    fun previewAudio(file: String) {
        val context = getApplication<Application>().applicationContext
        AzanAudioPlayer.setVolumePercent(_uiState.value.azanSettings.audioVolumePercent)
        AzanAudioPlayer.playAssetAudio(context, file) {
            _uiState.update { it.copy(isAzanAudioPlaying = false) }
        }
        _uiState.update { it.copy(isAzanAudioPlaying = true) }
    }

    fun resetDefaults() {
        persist(AzanSettings())
    }

    fun setTadaburNotificationEnabled(enabled: Boolean) {
        val updated = _uiState.value.tadaburSettings.copy(notificationEnabled = enabled)
        persistTadabur(updated)
    }

    fun setTadaburOffsetMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(5, 180)
        val updated = _uiState.value.tadaburSettings.copy(offsetMinutesAfterFajr = clamped)
        persistTadabur(updated)
    }

    private fun persist(settings: AzanSettings) {
        store.save(settings)
        _uiState.update {
            it.copy(
                azanSettings = settings,
                isAzanAudioPlaying = AzanAudioPlayer.isPlaying()
            )
        }
    }

    private fun persistTadabur(settings: TadaburSettings) {
        tadaburStore.save(settings)
        if (settings.notificationEnabled) {
            TadaburAlarmScheduler.scheduleFromAzan(
                context = getApplication<Application>().applicationContext,
                data = null,
                offsetMinutes = settings.offsetMinutesAfterFajr
            )
        } else {
            TadaburAlarmScheduler.cancel(getApplication<Application>().applicationContext)
        }
        _uiState.update {
            it.copy(tadaburSettings = settings)
        }
    }
}
