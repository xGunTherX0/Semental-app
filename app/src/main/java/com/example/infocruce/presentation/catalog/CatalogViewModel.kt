package com.example.infocruce.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HorseSex
import com.example.infocruce.domain.model.UserRole
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
    val role: UserRole = UserRole.OWNER,
    val error: String? = null
)

class CatalogViewModel(
    private val repository: HorseRepository,
    private val session: AppSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.currentUser.collect { user ->
                _uiState.update { it.copy(role = user.role) }
                loadCatalog()
            }
        }
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val role = session.currentUser.value.role
            // El dueño explora reproductores ajenos; el admin y el veterinario
            // necesitan visibilidad global de todos los caballos del sistema.
            val horses = if (role == UserRole.OWNER) {
                repository.getPublicCatalog()
            } else {
                repository.getAllHorses()
            }
            _uiState.update { state ->
                val filtered = if (state.selectedFilter == null) {
                    horses
                } else {
                    horses.filter { it.sex == state.selectedFilter }
                }
                state.copy(horses = horses, filteredHorses = filtered, isLoading = false)
            }
        }
    }

    fun filterBySex(sex: HorseSex?) {
        _uiState.update { state ->
            val filtered = if (sex == null) state.horses else state.horses.filter { it.sex == sex }
            state.copy(filteredHorses = filtered, selectedFilter = sex)
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    class Factory(
        private val repository: HorseRepository,
        private val session: AppSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CatalogViewModel(repository, session) as T
        }
    }
}
