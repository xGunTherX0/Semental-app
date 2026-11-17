package com.example.infocruce.presentation.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HorseSex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerUiState(
    val isSaving: Boolean = false,
    val savedHorse: Horse? = null,
    val error: String? = null
)

/**
 * Vista modelo del registro de caballos del dueño (perfil OWNER).
 */
class OwnerViewModel(
    private val repository: HorseRepository,
    private val session: AppSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerUiState())
    val uiState: StateFlow<OwnerUiState> = _uiState.asStateFlow()

    fun registerHorse(
        name: String,
        registrationNumber: String,
        sex: HorseSex,
        height: Int,
        temperament: String,
        isDnaVerified: Boolean,
        isPublicCatalog: Boolean
    ) {
        val trimmedName = name.trim()
        val trimmedRegistration = registrationNumber.trim()

        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(error = "El nombre del caballo es obligatorio") }
            return
        }
        if (trimmedRegistration.isEmpty()) {
            _uiState.update { it.copy(error = "El número de registro genealógico es obligatorio") }
            return
        }
        if (height !in MIN_HEIGHT..MAX_HEIGHT) {
            _uiState.update { it.copy(error = "La altura debe estar entre $MIN_HEIGHT y $MAX_HEIGHT cm") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val user = session.currentUser.value
                val horse = repository.addHorse(
                    Horse(
                        id = 0L,
                        ownerId = user.id,
                        name = trimmedName,
                        registrationNumber = trimmedRegistration,
                        sex = sex,
                        sireId = null,
                        damId = null,
                        height = height,
                        temperament = temperament.trim().ifEmpty { "Sin definir" },
                        isDnaVerified = isDnaVerified,
                        isPublicCatalog = isPublicCatalog,
                        photoUrl = null
                    )
                )
                _uiState.update { it.copy(isSaving = false, savedHorse = horse) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "No se pudo registrar el caballo"
                    )
                }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    fun resetForm() { _uiState.update { it.copy(savedHorse = null, error = null) } }

    class Factory(
        private val repository: HorseRepository,
        private val session: AppSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OwnerViewModel(repository, session) as T
        }
    }

    companion object {
        const val MIN_HEIGHT = 120
        const val MAX_HEIGHT = 220
    }
}
