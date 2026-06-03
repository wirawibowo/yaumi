package com.yaumi.app.tadabur.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.tadabur.data.TadaburProgressStore
import com.yaumi.app.tadabur.data.TadaburRepository
import com.yaumi.app.tadabur.notifications.TadaburNotificationHelper
import com.yaumi.app.tadabur.domain.TadaburDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TadaburUiState(
    val isLoading: Boolean = false,
    val dayIndex: Int = 1,
    val data: TadaburDay? = null,
    val checklist: List<Boolean> = emptyList(),
    val isCompleted: Boolean = false
)

class TadaburViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TadaburRepository(application.applicationContext)
    private val progress = TadaburProgressStore(application.applicationContext)
    private val _uiState = MutableStateFlow(TadaburUiState(isLoading = true))
    val uiState: StateFlow<TadaburUiState> = _uiState.asStateFlow()

    init {
        loadToday()
    }

    fun loadToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val total = repository.getAll().size
            val dayIndex = progress.getCurrentDayIndex(total)
            val data = repository.getByDay(dayIndex)
            val size = data?.amalTracker?.size ?: 0
            val checklist = progress.loadChecklist(dayIndex, size)
            val completed = progress.isDayCompleted(dayIndex)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    dayIndex = dayIndex,
                    data = data,
                    checklist = checklist,
                    isCompleted = completed
                )
            }
            progress.setLastOpenedDay(dayIndex)
        }
    }

    fun toggleChecklist(index: Int, checked: Boolean) {
        val current = _uiState.value
        val updated = current.checklist.toMutableList()
        if (index in updated.indices) {
            updated[index] = checked
            progress.saveChecklist(current.dayIndex, updated)
            _uiState.update { it.copy(checklist = updated) }
        }
    }

    fun markCompleted() {
        val current = _uiState.value
        progress.markDayCompleted(current.dayIndex)
        _uiState.update { it.copy(isCompleted = true) }
    }

    fun skipToday() {
        viewModelScope.launch {
            val total = repository.getAll().size
            progress.skipToday(total)
            val current = _uiState.value
            TadaburNotificationHelper.dismissForDay(
                getApplication<Application>().applicationContext,
                current.dayIndex
            )
            loadToday()
        }
    }
}
