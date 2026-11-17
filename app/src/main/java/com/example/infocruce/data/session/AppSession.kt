package com.example.infocruce.data.session

import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.domain.model.User
import com.example.infocruce.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesión simulada de InfoCruza: mantiene el usuario activo y su rol.
 *
 * Sustituye al login real. Igual que el repositorio, es un singleton
 * creado en [com.example.infocruce.InfoCruzaApplication], de modo que el
 * rol activo es consistente en toda la app.
 */
class AppSession(private val repository: HorseRepository) {

    private val _currentUser = MutableStateFlow(repository.getUserForRole(UserRole.OWNER))

    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    val currentRole: UserRole get() = _currentUser.value.role

    /**
     * Cambia el perfil activo (login simulado).
     */
    fun selectRole(role: UserRole) {
        _currentUser.value = repository.getUserForRole(role)
    }
}
