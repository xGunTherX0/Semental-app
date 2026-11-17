package com.example.infocruce.domain.model

/**
 * Perfiles de acceso al sistema InfoCruza.
 *
 * ADMIN: control total, visibilidad global de usuarios, criaderos y caballos.
 * OWNER: alta y registro de sus propios caballos en su criadero.
 * VET: registro de extracción de muestras fecales/bacterianas y emisión del semáforo de análisis.
 */
enum class UserRole(
    val displayName: String,
    val description: String,
    val canRegisterHorses: Boolean = false,
    val canRegisterSamples: Boolean = false,
    val canEmitAnalysis: Boolean = false,
    val canSeeGlobalData: Boolean = false
) {
    ADMIN(
        displayName = "Administrador",
        description = "Control total: visibilidad global de usuarios, criaderos y caballos",
        canSeeGlobalData = true
    ),
    OWNER(
        displayName = "Dueño",
        description = "Alta y registro de sus propios caballos en su criadero",
        canRegisterHorses = true
    ),
    VET(
        displayName = "Veterinario",
        description = "Registro de muestras fecales/bacterianas y emisión del semáforo de análisis",
        canRegisterSamples = true,
        canEmitAnalysis = true
    )
}
