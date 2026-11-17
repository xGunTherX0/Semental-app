package com.example.infocruce.domain.model

/**
 * Modelo de dominio que representa el semáforo de análisis emitido por el veterinario.
 *
 * Pensado para persistencia local (Room offline-first): campos planos,
 * tipos primitivos y sin dependencias de Android ni de coroutines.
 *
 * @param id Identificador único del informe
 * @param horseId ID del caballo analizado
 * @param sampleId ID de la muestra sobre la que se emite, si existe
 * @param trafficLight Semáforo del análisis
 * @param summary Resumen legible del análisis
 * @param createdAt Fecha de emisión en epoch millis
 */
data class LabReport(
    val id: Long,
    val horseId: Long,
    val sampleId: Long?,
    val trafficLight: TrafficLightStatus,
    val summary: String,
    val createdAt: Long
)
