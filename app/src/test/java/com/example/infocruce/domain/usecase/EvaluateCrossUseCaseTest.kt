package com.example.infocruce.domain.usecase

import com.example.infocruce.data.repository.InMemoryHorseRepository
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HorseSex
import com.example.infocruce.domain.model.TrafficLightStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del cálculo de compatibilidad genética.
 */
class EvaluateCrossUseCaseTest {

    private val repository = InMemoryHorseRepository()
    private val useCase = EvaluateCrossUseCase(repository)

    @Test
    fun `el mismo caballo no puede evaluarse consigo mismo`() {
        val error = runCatching { useCase(1L, 1L) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertNotNull(error?.message)
        assertEquals("No se puede evaluar un caballo consigo mismo", error?.message)
    }

    @Test
    fun `un macho inexistente informa de un mensaje no nulo`() {
        val error = runCatching { useCase(999L, 2L) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertNotNull(error?.message)
        assertEquals("El macho con ID 999 no existe en el catálogo", error?.message)
    }

    @Test
    fun `una hembra inexistente informa de un mensaje no nulo`() {
        val error = runCatching { useCase(1L, 999L) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertNotNull(error?.message)
        assertEquals("La hembra con ID 999 no existe en el catálogo", error?.message)
    }

    @Test
    fun `una hembra no puede actuar como sire`() {
        val error = runCatching { useCase(2L, 1L) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertNotNull(error?.message)
        assertEquals("Estrella del Sur no es un macho (semental)", error?.message)
    }

    @Test
    fun `un macho no puede actuar como dam`() {
        val error = runCatching { useCase(1L, 3L) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertNotNull(error?.message)
        assertEquals("Trueno Dorado no es una hembra (yegua)", error?.message)
    }

    @Test
    fun `una pareja con ADN y salud verificados resulta compatible`() {
        val result = useCase(3L, 8L)

        assertEquals(TrafficLightStatus.VERDE, result.trafficLightStatus)
        assertEquals(0.0, result.coiPercentage, 0.0001)
        assertEquals("Trueno Dorado", result.sireName)
        assertEquals("Brisa de Montaña", result.damName)
    }

    @Test
    fun `una pareja con ancestros comunes genera observacion y coi positivo`() {
        val hijo = repository.addHorse(
            Horse(
                id = 0L, ownerId = 1L, name = "Hijo Común", registrationNumber = "AR-2024-0001",
                sex = HorseSex.MACHO, sireId = 3L, damId = 2L, height = 160, temperament = "Tranquilo"
            )
        )
        val hija = repository.addHorse(
            Horse(
                id = 0L, ownerId = 1L, name = "Hija Común", registrationNumber = "AR-2024-0002",
                sex = HorseSex.HEMBRA, sireId = 3L, damId = 4L, height = 158, temperament = "Tranquilo"
            )
        )

        val result = useCase(hijo.id, hija.id)

        assertTrue(result.coiPercentage > 0.0)
        assertTrue(result.observations.any { it.contains("1 ancestro(s) común(es) encontrado(s)") })
    }

    @Test
    fun `sin registros sanitarios el semaforo es de precaucion`() {
        val result = useCase(1L, 2L)

        assertEquals(TrafficLightStatus.AMARILLO, result.trafficLightStatus)
        assertTrue(result.observations.any { it.contains("Sin registros sanitarios") })
    }
}
