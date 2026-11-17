package com.example.infocruce.data.repository

import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HorseSex
import com.example.infocruce.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del contrato de acceso a datos (la capa que Room sustituira).
 */
class HorseRepositoryTest {

    private val repository = InMemoryHorseRepository()

    @Test
    fun `los caballos del dueno se recuperan por propietario`() {
        assertEquals(3, repository.getMyHorses(1L).size)
        assertEquals(2, repository.getMyHorses(2L).size)
    }

    @Test
    fun `el catalogo publico solo expone los caballos marcados`() {
        val catalog = repository.getPublicCatalog()

        assertEquals(6, catalog.size)
        assertTrue(catalog.all { it.isPublicCatalog })
    }

    @Test
    fun `todos los caballos del sistema son visibles para el administrador`() {
        assertEquals(8, repository.getAllHorses().size)
    }

    @Test
    fun `anadir un caballo le asigna identificador`() {
        val added = repository.addHorse(
            Horse(
                id = 0L, ownerId = 1L, name = "Nube Gris", registrationNumber = "AR-2026-0001",
                sex = HorseSex.HEMBRA, height = 157, temperament = "Tranquilo"
            )
        )

        assertTrue(added.id > 0L)
        assertNotNull(repository.getHorseById(added.id))
        assertEquals("Nube Gris", repository.getHorseById(added.id)?.name)
        assertEquals(4, repository.getMyHorses(1L).size)
    }

    @Test
    fun `un caballo inexistente devuelve null`() {
        assertEquals(null, repository.getHorseById(9999L))
    }

    @Test
    fun `el repositorio expone los registros sanitarios`() {
        assertEquals(2, repository.getHealthRecords(3L).size)
        assertEquals(1, repository.getHealthRecords(1L).size)
        assertEquals(0, repository.getHealthRecords(2L).size)
    }

    @Test
    fun `el repositorio expone usuarios muestras e informes`() {
        assertEquals(6, repository.getUsers().size)
        assertEquals(3, repository.getAllSamples().size)
        assertEquals(2, repository.getAllReports().size)
        assertEquals(UserRole.VET, repository.getUserForRole(UserRole.VET).role)
    }

    @Test
    fun `registrar una muestra la suma al historial del caballo`() {
        val before = repository.getSamples(3L).size

        repository.addSample(
            com.example.infocruce.domain.model.Sample(
                id = 0L,
                horseId = 3L,
                type = com.example.infocruce.domain.model.SampleType.FECAL,
                status = com.example.infocruce.domain.model.HealthStatus.LIBRE,
                collectedAt = 0L
            )
        )

        assertEquals(before + 1, repository.getSamples(3L).size)
    }
}
