package com.example.infocruce.presentation.vet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.HealthStatus
import com.example.infocruce.domain.model.SampleType
import com.example.infocruce.domain.model.TrafficLightStatus
import com.example.infocruce.presentation.simulator.TrafficLightColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Registro de muestras y emisión del semáforo de análisis (perfil VET).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetSamplesScreen(
    onBackClick: () -> Unit,
    repository: HorseRepository,
    session: AppSession,
    viewModel: VetViewModel = viewModel(factory = VetViewModel.Factory(repository, session))
) {
    val uiState by viewModel.uiState.collectAsState()

    var sampleType by remember { mutableStateOf(SampleType.FECAL) }
    var sampleStatus by remember { mutableStateOf(HealthStatus.LIBRE) }
    var notes by remember { mutableStateOf("") }
    var horsesExpanded by remember { mutableStateOf(false) }

    val selectedHorse = uiState.horses.firstOrNull { it.id == uiState.selectedHorseId }
    val currentLight = uiState.currentLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muestras y análisis") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    }
                }
            }

            uiState.message?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Selector de caballo
            Text("Caballo analizado", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedButton(
                    onClick = { horsesExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedHorse?.name ?: "Sin caballos disponibles",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = horsesExpanded,
                    onDismissRequest = { horsesExpanded = false }
                ) {
                    uiState.horses.forEach { horse ->
                        DropdownMenuItem(
                            text = { Text("${horse.name} (${horse.registrationNumber})") },
                            onClick = {
                                viewModel.selectHorse(horse.id)
                                horsesExpanded = false
                            }
                        )
                    }
                }
            }

            // Semáforo
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val color = when (currentLight) {
                        TrafficLightStatus.VERDE -> TrafficLightColors.Verde
                        TrafficLightStatus.AMARILLO -> TrafficLightColors.Amarillo
                        TrafficLightStatus.ROJO -> TrafficLightColors.Rojo
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentLight) {
                                TrafficLightStatus.VERDE -> Icons.Default.Check
                                TrafficLightStatus.AMARILLO -> Icons.Default.Warning
                                TrafficLightStatus.ROJO -> Icons.Default.Close
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when (currentLight) {
                            TrafficLightStatus.VERDE -> "APTO"
                            TrafficLightStatus.AMARILLO -> "PRECAUCIÓN"
                            TrafficLightStatus.ROJO -> "NO APTO"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.emitAnalysis() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Emitir informe de análisis")
                    }
                }
            }

            // Muestras del caballo
            Text("Muestras registradas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (uiState.samples.isEmpty()) {
                Text(
                    text = "Sin muestras para este caballo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.samples.forEach { sample ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = "${sample.type.name} - ${sample.status.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Extracción: ${formatDate(sample.collectedAt)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (sample.notes.isNotBlank()) {
                                Text(
                                    text = sample.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Alta de muestra
            Text("Nueva extracción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SampleType.entries.forEach { type ->
                    FilterChip(
                        selected = sampleType == type,
                        onClick = { sampleType = type },
                        label = { Text(type.name) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HealthStatus.entries.forEach { status ->
                    FilterChip(
                        selected = sampleStatus == status,
                        onClick = { sampleStatus = status },
                        label = { Text(status.name) }
                    )
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    viewModel.registerSample(sampleType, sampleStatus, notes)
                    notes = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar muestra")
            }

            // Informes emitidos
            Text("Informes emitidos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (uiState.reports.isEmpty()) {
                Text(
                    text = "Sin informes para este caballo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.reports.forEach { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = report.trafficLight.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (report.trafficLight) {
                                    TrafficLightStatus.VERDE -> TrafficLightColors.Verde
                                    TrafficLightStatus.AMARILLO -> TrafficLightColors.Amarillo
                                    TrafficLightStatus.ROJO -> TrafficLightColors.Rojo
                                }
                            )
                            Text(text = report.summary, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Emitido: ${formatDate(report.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(epochMillis))
