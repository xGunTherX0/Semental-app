package com.example.infocruce.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.HealthRecord
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Resumen de un criadero (conjunto de caballos de un propietario). */
data class HerdSummary(
    val owner: User,
    val horses: List<Horse>
)

/** Fila global de un caballo con su propietario y sus registros sanitarios. */
data class AdminHorseRow(
    val horse: Horse,
    val ownerName: String,
    val health: List<HealthRecord>
)

data class AdminUiState(
    val users: List<User> = emptyList(),
    val herds: List<HerdSummary> = emptyList(),
    val horses: List<AdminHorseRow> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Vista modelo del panel de control global (perfil ADMIN):
 * usuarios, criaderos y caballos de todo el sistema.
 */
class AdminViewModel(
    private val repository: HorseRepository,
    private val session: AppSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val users = repository.getUsers()
            val horses = repository.getAllHorses()
            val herds = users.mapNotNull { owner ->
                val ownerHorses = horses.filter { it.ownerId == owner.id }
                if (ownerHorses.isEmpty()) null else HerdSummary(owner, ownerHorses)
            }
            val rows = horses.map { horse ->
                AdminHorseRow(
                    horse = horse,
                    ownerName = users.firstOrNull { it.id == horse.ownerId }?.name ?: "Desconocido",
                    health = repository.getHealthRecords(horse.id)
                )
            }
            _uiState.update {
                it.copy(users = users, herds = herds, horses = rows, isLoading = false)
            }
        }
    }

    class Factory(
        private val repository: HorseRepository,
        private val session: AppSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(repository, session) as T
        }
    }
}
