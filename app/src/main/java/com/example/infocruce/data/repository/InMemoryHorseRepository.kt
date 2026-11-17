package com.example.infocruce.data.repository

import com.example.infocruce.domain.model.*

class InMemoryHorseRepository {

    private val horses = mutableListOf<Horse>()
    private val healthRecords = mutableListOf<HealthRecord>()
    private val simulations = mutableListOf<CrossSimulationResult>()

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
    }

    fun getMyHorses(ownerId: Long): List<Horse> = horses.filter { it.ownerId == ownerId }

    fun getHorseById(id: Long): Horse? = horses.find { it.id == id }

    fun getPublicCatalog(): List<Horse> = horses.filter { it.isPublicCatalog }

    fun getHealthRecords(horseId: Long): List<HealthRecord> = healthRecords.filter { it.horseId == horseId }

    fun getAllAncestors(): List<Horse> = horses

    fun saveSimulationResult(result: CrossSimulationResult): CrossSimulationResult {
        simulations.add(result)
        return result
    }
}
