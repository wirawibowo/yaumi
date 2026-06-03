package com.yaumi.app.doa.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.doa.data.DoaRepository
import com.yaumi.app.doa.domain.model.DoaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val doaItems: List<DoaItem> = emptyList()
)

class DoaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DoaRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(DoaUiState())
    val uiState: StateFlow<DoaUiState> = _uiState.asStateFlow()

    init {
        loadDoa()
    }

    fun loadDoa() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getDoaList() }
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, doaItems = list) } }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Gagal memuat doa")
                    }
                }
        }
    }
}
