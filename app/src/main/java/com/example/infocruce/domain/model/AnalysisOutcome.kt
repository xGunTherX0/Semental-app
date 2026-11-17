package com.example.infocruce.domain.model

/**
 * Resultado del análisis de muestras de un caballo.
 *
 * @param trafficLight Semáforo emitido por el veterinario
 * @param summary Resumen legible del análisis
 */
data class AnalysisOutcome(
    val trafficLight: TrafficLightStatus,
    val summary: String
)
