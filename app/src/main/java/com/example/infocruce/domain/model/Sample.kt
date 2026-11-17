package com.example.infocruce.domain.model

/**
 * Tipo de muestra extraída por el veterinario.
 */
enum class SampleType {
    FECAL,
    BACTERIANA
}

/**
 * Modelo de dominio que representa una muestra extraída a un caballo.
 *
 * Pensado para persistencia local (Room offline-first): campos planos,
 * tipos primitivos y sin dependencias de Android ni de coroutines.
 *
 * @param id Identificador único de la muestra
 * @param horseId ID del caballo del que se extrajo la muestra
 * @param type Tipo de muestra tomada
 * @param status Resultado de laboratorio de la muestra
 * @param collectedAt Fecha de extracción en epoch millis
 * @param notes Observaciones del veterinario
 */
data class Sample(
    val id: Long,
    val horseId: Long,
    val type: SampleType,
    val status: HealthStatus,
    val collectedAt: Long,
    val notes: String = ""
)
