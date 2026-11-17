package com.example.infocruce.data.repository

import com.example.infocruce.domain.model.*

/**
 * Implementación en memoria del contrato [HorseRepository].
 *
 * Sirve de doble hasta que exista la capa Room: aporta datos semilla
 * deterministas y permite ejercitar la UI sin backend.
 */
class InMemoryHorseRepository : HorseRepository {

    private val horses = mutableListOf<Horse>()
    private val healthRecords = mutableListOf<HealthRecord>()
    private val simulations = mutableListOf<CrossSimulationResult>()
    private val users = mutableListOf<User>()
    private val samples = mutableListOf<Sample>()
    private val reports = mutableListOf<LabReport>()

    init {
        seedData()
    }

    private fun seedData() {
        val demoHorses = listOf(
            Horse(1, 1, "Relámpago del Valle", "AR-2020-1001", HorseSex.MACHO, null, null, 162, "Enérgico", true, false, null),
            Horse(2, 1, "Estrella del Sur", "AR-2020-1002", HorseSex.HEMBRA, null, null, 158, "Tranquilo", true, false, null),
            Horse(3, 2, "Trueno Dorado", "AR-2019-2001", HorseSex.MACHO, null, null, 165, "Enérgico", true, true, null),
            Horse(4, 2, "Luna Plateada", "AR-2021-2002", HorseSex.HEMBRA, null, null, 155, "Tranquilo", true, true, null),
            Horse(5, 3, "Tornado Rojo", "AR-2018-3001", HorseSex.MACHO, 3, null, 160, "Agitado", false, true, null),
            Horse(6, 3, "Cielo Azul", "AR-2022-3002", HorseSex.HEMBRA, null, 4, 157, "Tranquilo", true, true, null),
            Horse(7, 1, "Furia del Viento", "AR-2021-1003", HorseSex.MACHO, 1, 2, 163, "Enérgico", false, true, null),
            Horse(8, 4, "Brisa de Montaña", "AR-2020-4001", HorseSex.HEMBRA, null, null, 156, "Tranquilo", true, true, null),
        )
        horses.addAll(demoHorses)

        healthRecords.addAll(listOf(
            HealthRecord(1, 3, "Anemia Infecciosa Equina", HealthStatus.LIBRE),
            HealthRecord(2, 4, "Anemia Infecciosa Equina", HealthStatus.PORTADOR),
            HealthRecord(3, 6, "Anemia Infecciosa Equina", HealthStatus.LIBRE),
            HealthRecord(4, 1, "Difteria Equina", HealthStatus.LIBRE),
            HealthRecord(5, 5, "Difteria Equina", HealthStatus.PORTADOR),
            HealthRecord(6, 8, "Anemia Infecciosa Equina", HealthStatus.LIBRE),
            HealthRecord(7, 3, "Metritis Equina", HealthStatus.LIBRE),
        ))

        // El usuario OWNER demo es el id 1: conserva el comportamiento actual
        // de "mis caballos" (getMyHorses(1L)) sin cambiar los datos semilla.
        users.addAll(listOf(
            User(1, "Camila Alarcón", "camila@infocruce.app", UserRole.OWNER),
            User(2, "Joaquín Herrera", "joaquin@infocruce.app", UserRole.OWNER),
            User(3, "Marta Ruiz", "marta@infocruce.app", UserRole.OWNER),
            User(4, "Nicolás Soto", "nicolas@infocruce.app", UserRole.OWNER),
            User(100, "Dra. Elena Vidal", "elena@infocruce.app", UserRole.VET),
            User(200, "Ing. Pablo León", "pablo@infocruce.app", UserRole.ADMIN),
        ))

        samples.addAll(listOf(
            Sample(1, 3, SampleType.FECAL, HealthStatus.PORTADOR, 1_757_000_000_000L, "Control anual de Anemia Infecciosa Equina"),
            Sample(2, 6, SampleType.BACTERIANA, HealthStatus.LIBRE, 1_757_200_000_000L, "Bacteriología de rutina"),
            Sample(3, 5, SampleType.FECAL, HealthStatus.AFECTADO, 1_757_400_000_000L, "Difteria equina confirmada"),
        ))

        reports.addAll(listOf(
            LabReport(1, 3, 1, TrafficLightStatus.AMARILLO, "Portador de Anemia Infecciosa Equina", 1_757_100_000_000L),
            LabReport(2, 5, 3, TrafficLightStatus.ROJO, "Difteria equina en estado afectado", 1_757_500_000_000L),
        ))
    }

    override fun getMyHorses(ownerId: Long): List<Horse> = horses.filter { it.ownerId == ownerId }

    override fun getHorseById(id: Long): Horse? = horses.find { it.id == id }

    override fun getPublicCatalog(): List<Horse> = horses.filter { it.isPublicCatalog }

    override fun getAllHorses(): List<Horse> = horses.toList()

    override fun addHorse(horse: Horse): Horse {
        val newHorse = if (horse.id > 0L) horse else horse.copy(id = nextHorseId())
        horses.add(newHorse)
        return newHorse
    }

    override fun getHealthRecords(horseId: Long): List<HealthRecord> = healthRecords.filter { it.horseId == horseId }

    override fun getUsers(): List<User> = users.toList()

    override fun getUserForRole(role: UserRole): User =
        users.firstOrNull { it.role == role } ?: users.first()

    override fun saveSimulationResult(result: CrossSimulationResult): CrossSimulationResult {
        simulations.add(result)
        return result
    }

    override fun getSimulationResults(): List<CrossSimulationResult> = simulations.toList()

    override fun getSamples(horseId: Long): List<Sample> = samples.filter { it.horseId == horseId }

    override fun getAllSamples(): List<Sample> = samples.toList()

    override fun addSample(sample: Sample): Sample {
        val newSample = if (sample.id > 0L) sample else sample.copy(id = nextSampleId())
        samples.add(newSample)
        return newSample
    }

    override fun getReports(horseId: Long): List<LabReport> = reports.filter { it.horseId == horseId }

    override fun getAllReports(): List<LabReport> = reports.toList()

    override fun addReport(report: LabReport): LabReport {
        val newReport = if (report.id > 0L) report else report.copy(id = nextReportId())
        reports.add(newReport)
        return newReport
    }

    private fun nextHorseId(): Long = (horses.maxOfOrNull { it.id } ?: 0L) + 1

    private fun nextSampleId(): Long = (samples.maxOfOrNull { it.id } ?: 0L) + 1

    private fun nextReportId(): Long = (reports.maxOfOrNull { it.id } ?: 0L) + 1
}
