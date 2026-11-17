package com.example.infocruce.domain.model

/**
 * Enumeración de roles disponibles en el sistema InfoCruza.
 */
enum class UserRole {
    CRIADOR,
    VETERINARIO,
    ADMINISTRADOR
}

/**
 * Modelo de dominio que representa a un usuario del sistema.
 *
 * @param id Identificador único del usuario
 * @param name Nombre completo
 * @param email Correo electrónico (usado como identificador de login)
 * @param role Rol del usuario en el sistema
 */
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole
)
