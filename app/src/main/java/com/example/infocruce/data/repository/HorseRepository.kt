package com.example.infocruce.data.repository

import com.example.infocruce.domain.model.CrossSimulationResult
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HealthRecord
import com.example.infocruce.domain.model.LabReport
import com.example.infocruce.domain.model.Sample
import com.example.infocruce.domain.model.User
import com.example.infocruce.domain.model.UserRole

/**
 * Contrato de acceso a datos de InfoCruza.
 *
 * Hoy lo implementa [InMemoryHorseRepository]. Al preparar la persistencia
 * local (Room offline-first) basta con añadir una implementación backed por
 * base de datos: el dominio y la presentación no cambian.
 */
interface HorseRepository {

    // Caballos
    fun getMyHorses(ownerId: Long): List<Horse>
    fun getHorseById(id: Long): Horse?
    fun getPublicCatalog(): List<Horse>
    fun getAllHorses(): List<Horse>
    fun addHorse(horse: Horse): Horse

    // Salud
    fun getHealthRecords(horseId: Long): List<HealthRecord>

    // Usuarios
    fun getUsers(): List<User>
    fun getUserForRole(role: UserRole): User

    // Simulaciones de cruce
    fun saveSimulationResult(result: CrossSimulationResult): CrossSimulationResult
    fun getSimulationResults(): List<CrossSimulationResult>

    // Veterinario: muestras y semáforo de análisis
    fun getSamples(horseId: Long): List<Sample>
    fun getAllSamples(): List<Sample>
    fun addSample(sample: Sample): Sample
    fun getReports(horseId: Long): List<LabReport>
    fun getAllReports(): List<LabReport>
    fun addReport(report: LabReport): LabReport
}
