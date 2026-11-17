package com.example.infocruce.domain.model

/**
 * Enumeración del estado sanitario/genético de una condición.
 */
enum class HealthStatus {
    LIBRE,
    PORTADOR,
    AFECTADO
}

/**
 * Modelo de dominio que representa un registro sanitario/genético de un caballo.
 *
 * @param id Identificador único del registro
 * @param horseId ID del caballo al que pertenece
 * @param conditionName Nombre de la condición (ej: "Anemia Infecciosa Equina", "CLE")
 * @param status Estado de la condición para este caballo
 */
data class HealthRecord(
    val id: Long,
    val horseId: Long,
    val conditionName: String,
    val status: HealthStatus
)
