package com.example.infocruce.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.infocruce.data.repository.InMemoryHorseRepository
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HorseSex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val horses: List<Horse> = emptyList(),
    val filteredHorses: List<Horse> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: HorseSex? = null,
    val error: String? = null
)

class CatalogViewModel(
    private val repository: InMemoryHorseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init { loadCatalog() }

    private fun loadCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val horses = repository.getPublicCatalog()
            _uiState.update { it.copy(horses = horses, filteredHorses = horses, isLoading = false) }
        }
    }

    fun filterBySex(sex: HorseSex?) {
        _uiState.update { state ->
            val filtered = if (sex == null) state.horses else state.horses.filter { it.sex == sex }
            state.copy(filteredHorses = filtered, selectedFilter = sex)
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    class Factory(private val repository: InMemoryHorseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CatalogViewModel(repository) as T
        }
    }
}
