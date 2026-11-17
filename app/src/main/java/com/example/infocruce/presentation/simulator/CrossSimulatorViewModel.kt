package com.example.infocruce.presentation.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.*
import com.example.infocruce.domain.usecase.EvaluateCrossUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimulatorUiState(
    val myHorses: List<Horse> = emptyList(),
    val publicHorses: List<Horse> = emptyList(),
    val selectedSire: Horse? = null,
    val selectedDam: Horse? = null,
    val isLoading: Boolean = false,
    val result: CrossSimulationResult? = null,
    val error: String? = null,
    val role: UserRole = UserRole.OWNER
)

class CrossSimulatorViewModel(
    private val repository: HorseRepository,
    private val session: AppSession,
    private val evaluateCrossUseCase: EvaluateCrossUseCase,
    private val preselectedHorseId: Long = -1L
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulatorUiState())
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.currentUser.collect { user ->
                _uiState.update { it.copy(role = user.role) }
                loadHorses()
            }
        }
        preselectHorse(preselectedHorseId)
    }

    /**
     * Preselecciona el caballo que llega desde el catálogo: si es macho ocupa
     * el slot de sire y si es hembra el de dam.
     */
    private fun preselectHorse(id: Long) {
        if (id <= 0L) return
        val horse = repository.getHorseById(id) ?: return
        when (horse.sex) {
            HorseSex.MACHO -> _uiState.update { it.copy(selectedSire = horse, result = null) }
            HorseSex.HEMBRA -> _uiState.update { it.copy(selectedDam = horse, result = null) }
        }
    }

    private fun loadHorses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = session.currentUser.value
            // El dueño trabaja con su criadero; admin y veterinario ven todos los caballos.
            val my = when (user.role) {
                UserRole.OWNER -> repository.getMyHorses(user.id)
                UserRole.ADMIN, UserRole.VET -> repository.getAllHorses()
            }
            val pub = repository.getPublicCatalog()
            _uiState.update { it.copy(myHorses = my, publicHorses = pub, isLoading = false) }
        }
    }

    fun selectSire(horse: Horse) { _uiState.update { it.copy(selectedSire = horse, result = null) } }
    fun selectDam(horse: Horse) { _uiState.update { it.copy(selectedDam = horse, result = null) } }

    fun evaluateCross() {
        val sire = _uiState.value.selectedSire
        val dam = _uiState.value.selectedDam
        if (sire == null || dam == null) {
            _uiState.update { it.copy(error = "Seleccione un macho y una hembra") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = evaluateCrossUseCase(sire.id, dam.id)
                repository.saveSimulationResult(result)
                _uiState.update { it.copy(isLoading = false, result = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo completar la evaluación del cruce"
                    )
                }
            }
        }
    }

    fun clearResult() { _uiState.update { it.copy(result = null, selectedSire = null, selectedDam = null, error = null) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun getMyMales(): List<Horse> = _uiState.value.myHorses.filter { it.sex == HorseSex.MACHO }
    fun getAvailableFemales(): List<Horse> =
        (_uiState.value.myHorses.filter { it.sex == HorseSex.HEMBRA } +
            _uiState.value.publicHorses.filter { it.sex == HorseSex.HEMBRA }).distinctBy { it.id }

    class Factory(
        private val repository: HorseRepository,
        private val session: AppSession,
        private val preselectedHorseId: Long = -1L
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrossSimulatorViewModel(
                repository = repository,
                session = session,
                evaluateCrossUseCase = EvaluateCrossUseCase(repository),
                preselectedHorseId = preselectedHorseId
            ) as T
        }
    }
}
