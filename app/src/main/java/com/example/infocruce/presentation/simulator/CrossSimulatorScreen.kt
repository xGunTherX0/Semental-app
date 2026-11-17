package com.example.infocruce.presentation.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.CrossSimulationResult
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.TrafficLightStatus

object TrafficLightColors {
    val Verde = Color(0xFF4CAF50)
    val Amarillo = Color(0xFFFFC107)
    val Rojo = Color(0xFFF44336)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossSimulatorScreen(
    onBackClick: () -> Unit,
    repository: HorseRepository,
    session: AppSession,
    preselectedHorseId: Long = -1L,
    viewModel: CrossSimulatorViewModel = viewModel(
        key = "simulator-$preselectedHorseId",
        factory = CrossSimulatorViewModel.Factory(repository, session, preselectedHorseId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectingSire by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulador de Cruce") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seleccione los reproductores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        HorseSelectionCard("Macho (Sire)", uiState.selectedSire, Modifier.weight(1f)) { selectingSire = true; showDialog = true }
                        Text("×", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                        HorseSelectionCard("Hembra (Dam)", uiState.selectedDam, Modifier.weight(1f)) { selectingSire = false; showDialog = true }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.evaluateCross() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedSire != null && uiState.selectedDam != null && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Analizar Compatibilidad")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            uiState.error?.let { error ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) { Icon(Icons.Default.Close, "Cerrar") }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            uiState.result?.let { result -> TrafficLightResultCard(result) { viewModel.clearResult() } }
        }
    }

    if (showDialog) {
        val horses = if (selectingSire) viewModel.getMyMales() else viewModel.getAvailableFemales()
        HorseSelectionDialog(
            if (selectingSire) "Seleccionar Macho" else "Seleccionar Hembra",
            horses,
            { horse -> if (selectingSire) viewModel.selectSire(horse) else viewModel.selectDam(horse); showDialog = false },
            { showDialog = false }
        )
    }
}

@Composable
fun HorseSelectionCard(label: String, horse: Horse?, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (horse != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            if (horse != null) {
                Text(horse.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(horse.registrationNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Toque para seleccionar", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun TrafficLightResultCard(result: CrossSimulationResult, onDismiss: () -> Unit) {
    val color = when (result.trafficLightStatus) {
        TrafficLightStatus.VERDE -> TrafficLightColors.Verde
        TrafficLightStatus.AMARILLO -> TrafficLightColors.Amarillo
        TrafficLightStatus.ROJO -> TrafficLightColors.Rojo
    }
    val statusText = when (result.trafficLightStatus) {
        TrafficLightStatus.VERDE -> "COMPATIBLE"
        TrafficLightStatus.AMARILLO -> "PRECAUCIÓN"
        TrafficLightStatus.ROJO -> "INCOMPATIBLE"
    }
    val icon = when (result.trafficLightStatus) {
        TrafficLightStatus.VERDE -> Icons.Default.Check
        TrafficLightStatus.AMARILLO -> Icons.Default.Warning
        TrafficLightStatus.ROJO -> Icons.Default.Close
    }

    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(100.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                Icon(icon, statusText, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(statusText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(8.dp))
            Text("${result.sireName} × ${result.damName}", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Coeficiente de Consanguinidad (COI)", style = MaterialTheme.typography.labelMedium)
                    Text("${String.format("%.2f", result.coiPercentage)}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (result.observations.isNotEmpty()) {
                Text("Observaciones:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                result.observations.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Nueva Simulación") }
        }
    }
}

@Composable
fun HorseSelectionDialog(title: String, horses: List<Horse>, onSelect: (Horse) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (horses.isEmpty()) Text("No hay caballos disponibles.")
            else LazyColumn { items(horses) { horse ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(horse) }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(horse.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Reg: ${horse.registrationNumber}", style = MaterialTheme.typography.bodySmall)
                            Text("Altura: ${horse.height}cm | ${horse.temperament}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (horse.isDnaVerified) Icon(Icons.Default.Check, "ADN Verificado", tint = TrafficLightColors.Verde)
                    }
                }
            } }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
