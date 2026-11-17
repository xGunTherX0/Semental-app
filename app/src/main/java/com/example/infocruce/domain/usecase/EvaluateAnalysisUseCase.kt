package com.example.infocruce.domain.usecase

import com.example.infocruce.domain.model.AnalysisOutcome
import com.example.infocruce.domain.model.HealthStatus
import com.example.infocruce.domain.model.Sample
import com.example.infocruce.domain.model.TrafficLightStatus

/**
 * Regla de semáforo del laboratorio veterinario.
 *
 * Sin muestras o sin incidencias -> VERDE.
 * Algún portador -> AMARILLO (requiere seguimiento).
 * Algún afectado -> ROJO (no apto para reproducción).
 */
class EvaluateAnalysisUseCase {

    operator fun invoke(samples: List<Sample>): AnalysisOutcome {
        val affected = samples.filter { it.status == HealthStatus.AFECTADO }
        val carriers = samples.filter { it.status == HealthStatus.PORTADOR }

        val trafficLight = when {
            affected.isNotEmpty() -> TrafficLightStatus.ROJO
            carriers.isNotEmpty() -> TrafficLightStatus.AMARILLO
            else -> TrafficLightStatus.VERDE
        }

        val summary = when {
            affected.isNotEmpty() -> "Afectado por: ${affected.joinToString { it.type.name }}"
            carriers.isNotEmpty() -> "Portador de: ${carriers.joinToString { it.type.name }}"
            else -> "Sin condiciones detectadas en las muestras registradas"
        }

        return AnalysisOutcome(trafficLight, summary)
    }
}
