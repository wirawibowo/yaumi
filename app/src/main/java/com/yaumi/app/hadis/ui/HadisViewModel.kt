package com.yaumi.app.hadis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.hadis.data.HadisRepository
import com.yaumi.app.hadis.domain.model.HadisCollection
import com.yaumi.app.hadis.domain.model.HadisItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HadisUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val collections: List<HadisCollection> = emptyList(),
    val selectedCollection: HadisCollection? = null,
    val hadisItems: List<HadisItem> = emptyList(),
    val pageSize: Int = 100,
    val visibleCount: Int = 50
)

class HadisViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HadisRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(HadisUiState())
    val uiState: StateFlow<HadisUiState> = _uiState.asStateFlow()

    init {
        loadCollections()
    }

    fun loadCollections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getCollections() }
                .onSuccess { list ->
                    val first = list.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            collections = list,
                            selectedCollection = first
                        )
                    }
                    first?.let { selectCollection(it.slug) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Gagal memuat hadis")
                    }
                }
        }
    }

    fun selectCollection(slug: String) {
        viewModelScope.launch {
            val selected = _uiState.value.collections.firstOrNull { it.slug == slug }
            _uiState.update { it.copy(isLoading = true, selectedCollection = selected, errorMessage = null) }
            runCatching { repository.getHadisBySlug(slug, limit = _uiState.value.pageSize) }
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hadisItems = items,
                            visibleCount = minOf(50, items.size)
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Gagal memuat isi hadis")
                    }
                }
        }
    }

    fun loadMore() {
        _uiState.update {
            val next = minOf(it.visibleCount + 50, it.hadisItems.size)
            it.copy(visibleCount = next)
        }
    }
}
