package com.example.infocruce.presentation.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.Horse
import com.example.infocruce.domain.model.HorseSex
import com.example.infocruce.domain.model.UserRole
import com.example.infocruce.presentation.simulator.TrafficLightColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onBackClick: () -> Unit,
    onEvaluateClick: (Long) -> Unit,
    repository: HorseRepository,
    session: AppSession,
    viewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory(repository, session))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.role == UserRole.OWNER) "Catálogo Público" else "Todos los caballos") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Volver") } },
                actions = { IconButton(onClick = { }) { Icon(Icons.Default.Refresh, "Actualizar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = uiState.selectedFilter == null, onClick = { viewModel.filterBySex(null) }, label = { Text("Todos") })
                FilterChip(selected = uiState.selectedFilter == HorseSex.MACHO, onClick = { viewModel.filterBySex(HorseSex.MACHO) }, label = { Text("Machos") })
                FilterChip(selected = uiState.selectedFilter == HorseSex.HEMBRA, onClick = { viewModel.filterBySex(HorseSex.HEMBRA) }, label = { Text("Hembras") })
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.horses.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("No hay caballos en el catálogo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.filteredHorses) { horse ->
                        HorseCatalogCard(horse) { onEvaluateClick(horse.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun HorseCatalogCard(horse: Horse, onEvaluateClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(80.dp).clip(MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                    if (horse.photoUrl != null) AsyncImage(model = horse.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Text(horse.name.first().toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(horse.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (horse.isDnaVerified) { Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Check, "ADN Verificado", tint = TrafficLightColors.Verde, modifier = Modifier.size(16.dp)) }
                    }
                    Text("Reg: ${horse.registrationNumber}", style = MaterialTheme.typography.bodySmall)
                    Text("Sexo: ${horse.sex.name} | Altura: ${horse.height}cm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Temperamento: ${horse.temperament}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!horse.isDnaVerified) Text("ADN Pendiente", style = MaterialTheme.typography.labelSmall, color = TrafficLightColors.Amarillo)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEvaluateClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text(if (horse.sex == HorseSex.MACHO) "Usar como macho (sire)" else "Usar como hembra (dam)")
            }
        }
    }
}
