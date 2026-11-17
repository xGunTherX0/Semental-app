package com.example.infocruce.domain.usecase

import com.example.infocruce.domain.model.HealthStatus
import com.example.infocruce.domain.model.Sample
import com.example.infocruce.domain.model.SampleType
import com.example.infocruce.domain.model.TrafficLightStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del semáforo de análisis del laboratorio veterinario.
 */
class EvaluateAnalysisUseCaseTest {

    private val useCase = EvaluateAnalysisUseCase()

    @Test
    fun `sin muestras el semaforo es verde`() {
        val outcome = useCase(emptyList())

        assertEquals(TrafficLightStatus.VERDE, outcome.trafficLight)
        assertTrue(outcome.summary.contains("Sin condiciones"))
    }

    @Test
    fun `un portador deja el semaforo en amarillo`() {
        val outcome = useCase(
            listOf(sample(1L, HealthStatus.PORTADOR, SampleType.FECAL))
        )

        assertEquals(TrafficLightStatus.AMARILLO, outcome.trafficLight)
        assertTrue(outcome.summary.contains("Portador de: FECAL"))
    }

    @Test
    fun `un afectado deja el semaforo en rojo`() {
        val outcome = useCase(
            listOf(
                sample(1L, HealthStatus.PORTADOR, SampleType.FECAL),
                sample(2L, HealthStatus.AFECTADO, SampleType.BACTERIANA)
            )
        )

        assertEquals(TrafficLightStatus.ROJO, outcome.trafficLight)
        assertTrue(outcome.summary.contains("Afectado por: BACTERIANA"))
    }

    @Test
    fun `una muestra libre no altera el semaforo`() {
        val outcome = useCase(
            listOf(sample(1L, HealthStatus.LIBRE, SampleType.FECAL))
        )

        assertEquals(TrafficLightStatus.VERDE, outcome.trafficLight)
    }

    private fun sample(id: Long, status: HealthStatus, type: SampleType) = Sample(
        id = id,
        horseId = 3L,
        type = type,
        status = status,
        collectedAt = 0L
    )
}
