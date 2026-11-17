package com.example.infocruce.presentation.vet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.HealthStatus
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.LabReport
import com.example.infocruce.domain.model.Sample
import com.example.infocruce.domain.model.SampleType
import com.example.infocruce.domain.model.TrafficLightStatus
import com.example.infocruce.domain.usecase.EvaluateAnalysisUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VetUiState(
    val horses: List<Horse> = emptyList(),
    val selectedHorseId: Long? = null,
    val samples: List<Sample> = emptyList(),
    val reports: List<LabReport> = emptyList(),
    val currentLight: TrafficLightStatus = TrafficLightStatus.VERDE,
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

/**
 * Vista modelo del laboratorio del veterinario (perfil VET):
 * registro de muestras fecal/bacteriana y emisión del semáforo de análisis.
 */
class VetViewModel(
    private val repository: HorseRepository,
    private val session: AppSession,
    private val evaluateAnalysis: EvaluateAnalysisUseCase = EvaluateAnalysisUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VetUiState())
    val uiState: StateFlow<VetUiState> = _uiState.asStateFlow()

    init {
        loadHorses()
    }

    private fun loadHorses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // El veterinario tiene visibilidad global de los caballos del sistema.
            val horses = repository.getAllHorses()
            val selected = _uiState.value.selectedHorseId ?: horses.firstOrNull()?.id
            _uiState.update { it.copy(horses = horses, selectedHorseId = selected, isLoading = false) }
            selected?.let { loadHorseData(it) }
        }
    }

    fun selectHorse(horseId: Long) {
        _uiState.update { it.copy(selectedHorseId = horseId) }
        loadHorseData(horseId)
    }

    private fun loadHorseData(horseId: Long) {
        val samples = repository.getSamples(horseId)
        _uiState.update {
            it.copy(
                samples = samples,
                reports = repository.getReports(horseId),
                currentLight = evaluateAnalysis(samples).trafficLight
            )
        }
    }

    fun registerSample(type: SampleType, status: HealthStatus, notes: String) {
        val horseId = _uiState.value.selectedHorseId
        if (horseId == null) {
            _uiState.update { it.copy(error = "Seleccione un caballo antes de registrar la muestra") }
            return
        }
        viewModelScope.launch {
            try {
                val sample = repository.addSample(
                    Sample(
                        id = 0L,
                        horseId = horseId,
                        type = type,
                        status = status,
                        collectedAt = System.currentTimeMillis(),
                        notes = notes.trim()
                    )
                )
                loadHorseData(horseId)
                _uiState.update {
                    it.copy(message = "Muestra de tipo ${sample.type.name} registrada")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "No se pudo registrar la muestra")
                }
            }
        }
    }

    /**
     * Emite el semáforo de análisis del caballo seleccionado a partir de sus muestras.
     */
    fun emitAnalysis() {
        val horseId = _uiState.value.selectedHorseId
        if (horseId == null) {
            _uiState.update { it.copy(error = "Seleccione un caballo antes de emitir el análisis") }
            return
        }
        val samples = _uiState.value.samples
        val outcome = evaluateAnalysis(samples)
        viewModelScope.launch {
            try {
                val report = repository.addReport(
                    LabReport(
                        id = 0L,
                        horseId = horseId,
                        sampleId = samples.lastOrNull()?.id,
                        trafficLight = outcome.trafficLight,
                        summary = outcome.summary,
                        createdAt = System.currentTimeMillis()
                    )
                )
                loadHorseData(horseId)
                _uiState.update {
                    it.copy(message = "Análisis emitido: ${report.trafficLight.name}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "No se pudo emitir el análisis")
                }
            }
        }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null) } }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    class Factory(
        private val repository: HorseRepository,
        private val session: AppSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VetViewModel(repository, session) as T
        }
    }
}
