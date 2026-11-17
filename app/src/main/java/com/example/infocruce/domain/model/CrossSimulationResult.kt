package com.example.infocruce.domain.model

/**
 * Modelo de dominio que representa el resultado de una simulación de cruce.
 *
 * @param sireId ID del macho (semental) evaluado
 * @param damId ID de la hembra (yegua) evaluada
 * @param coiPercentage Coeficiente de Consanguinidad (COI) calculado en porcentaje
 * @param trafficLightStatus Clasificación del semáforo de compatibilidad
 * @param observations Lista de observaciones y advertencias generadas
 * @param sireName Nombre del macho (para mostrar en UI)
 * @param damName Nombre de la hembra (para mostrar en UI)
 */
data class CrossSimulationResult(
    val sireId: Long,
    val damId: Long,
    val coiPercentage: Double,
    val trafficLightStatus: TrafficLightStatus,
    val observations: List<String>,
    val sireName: String = "",
    val damName: String = ""
)
