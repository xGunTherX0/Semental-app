package com.example.infocruce.domain.usecase

import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.domain.model.*
import java.util.Locale

class EvaluateCrossUseCase(
    private val repository: HorseRepository
) {

    operator fun invoke(sireId: Long, damId: Long): CrossSimulationResult {
        require(sireId != damId) { "No se puede evaluar un caballo consigo mismo" }

        val sire = repository.getHorseById(sireId)
            ?: throw IllegalArgumentException("El macho con ID $sireId no existe en el catálogo")
        val dam = repository.getHorseById(damId)
            ?: throw IllegalArgumentException("La hembra con ID $damId no existe en el catálogo")

        require(sire.sex == HorseSex.MACHO) { "${sire.name} no es un macho (semental)" }
        require(dam.sex == HorseSex.HEMBRA) { "${dam.name} no es una hembra (yegua)" }

        val sireAncestors = getAncestors(sire)
        val damAncestors = getAncestors(dam)
        val shared = sireAncestors.intersect(damAncestors)
        val maxGen = maxOf(getTreeDepth(sire), getTreeDepth(dam))
        val coi = calculateCOI(shared.size, maxGen)

        val sireHealth = repository.getHealthRecords(sire.id)
        val damHealth = repository.getHealthRecords(dam.id)
        val healthRisks = analyzeHealthRisks(sireHealth, damHealth)

        val observations = mutableListOf<String>()
        if (shared.isNotEmpty()) {
            observations.add("${shared.size} ancestro(s) común(es) encontrado(s)")
        }
        if (coi >= 5.0) {
            observations.add("COI del ${format(coi)}% — parentesco ${if (coi >= 10.0) "alto" else "moderado"}")
        }
        observations.addAll(healthRisks)
        if (sireHealth.isEmpty()) observations.add("Sin registros sanitarios para ${sire.name}")
        if (damHealth.isEmpty()) observations.add("Sin registros sanitarios para ${dam.name}")
        if (!sire.isDnaVerified) observations.add("ADN de ${sire.name} no verificado")
        if (!dam.isDnaVerified) observations.add("ADN de ${dam.name} no verificado")

        val trafficLight = when {
            coi >= 10.0 || healthRisks.any { it.contains("RIESGO") } -> TrafficLightStatus.ROJO
            coi in 5.0..9.9 || observations.any { it.contains("Sin registros") } -> TrafficLightStatus.AMARILLO
            else -> TrafficLightStatus.VERDE
        }

        return CrossSimulationResult(sireId, damId, coi, trafficLight, observations, sire.name, dam.name)
    }

    private fun getAncestors(horse: Horse, depth: Int = 0): Set<Long> {
        if (depth >= 5) return emptySet()
        val ancestors = mutableSetOf<Long>()
        horse.sireId?.let { id ->
            ancestors.add(id)
            repository.getHorseById(id)?.let { ancestors.addAll(getAncestors(it, depth + 1)) }
        }
        horse.damId?.let { id ->
            ancestors.add(id)
            repository.getHorseById(id)?.let { ancestors.addAll(getAncestors(it, depth + 1)) }
        }
        return ancestors
    }

    private fun getTreeDepth(horse: Horse, depth: Int = 0): Int {
        if (depth >= 5) return depth
        var max = depth
        horse.sireId?.let { repository.getHorseById(it)?.let { max = maxOf(max, getTreeDepth(it, depth + 1)) } }
        horse.damId?.let { repository.getHorseById(it)?.let { max = maxOf(max, getTreeDepth(it, depth + 1)) } }
        return max
    }

    private fun calculateCOI(sharedCount: Int, generations: Int): Double {
        if (sharedCount == 0) return 0.0
        val base = when (sharedCount) { 1 -> 3.125; 2 -> 6.25; 3 -> 9.375; else -> 12.5 }
        val factor = when { generations <= 2 -> 1.5; generations <= 3 -> 1.0; else -> 0.75 }
        return (base * factor).coerceAtMost(50.0)
    }

    private fun analyzeHealthRisks(sireHealth: List<HealthRecord>, damHealth: List<HealthRecord>): List<String> {
        val risks = mutableListOf<String>()
        val shared = sireHealth.map { it.conditionName }.toSet().intersect(damHealth.map { it.conditionName }.toSet())
        for (cond in shared) {
            val sStat = sireHealth.filter { it.conditionName == cond }.map { it.status }
            val dStat = damHealth.filter { it.conditionName == cond }.map { it.status }
            if ((HealthStatus.PORTADOR in sStat || HealthStatus.AFECTADO in sStat) &&
                (HealthStatus.PORTADOR in dStat || HealthStatus.AFECTADO in dStat)) {
                risks.add("RIESGO GENÉTICO: Ambos progenitores afectados de '$cond'")
            }
        }
        return risks
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.1f", value)
}
