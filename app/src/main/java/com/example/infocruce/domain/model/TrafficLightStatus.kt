package com.example.infocruce.domain.model

/**
 * Enumeración del estado del semáforo.
 *
 * Se usa tanto en la compatibilidad genética de un cruce como en el
 * semáforo de análisis emitido por el veterinario.
 */
enum class TrafficLightStatus {
    VERDE,
    AMARILLO,
    ROJO
}
