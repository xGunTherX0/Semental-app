package com.example.infocruce.presentation.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.session.AppSession
import com.example.infocruce.domain.model.HorseSex

/**
 * Alta de caballos en el criadero (perfil OWNER).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterHorseScreen(
    onBackClick: () -> Unit,
    repository: HorseRepository,
    session: AppSession,
    viewModel: OwnerViewModel = viewModel(factory = OwnerViewModel.Factory(repository, session))
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("160") }
    var temperament by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(HorseSex.MACHO) }
    var isDnaVerified by remember { mutableStateOf(false) }
    var isPublicCatalog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar caballo") },
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
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Check, "Cerrar")
                        }
                    }
                }
            }

            uiState.savedHorse?.let { horse ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Alta registrada",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("${horse.name} (${horse.registrationNumber})")
                        Text("Sexo: ${horse.sex.name} | Altura: ${horse.height} cm")
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.resetForm() }) { Text("Registrar otro") }
                            Button(onClick = onBackClick) { Text("Volver al inicio") }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = registrationNumber,
                onValueChange = { registrationNumber = it },
                label = { Text("Número de registro genealógico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Sexo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = sex == HorseSex.MACHO,
                    onClick = { sex = HorseSex.MACHO },
                    label = { Text("Macho") }
                )
                FilterChip(
                    selected = sex == HorseSex.HEMBRA,
                    onClick = { sex = HorseSex.HEMBRA },
                    label = { Text("Hembra") }
                )
            }

            OutlinedTextField(
                value = height,
                onValueChange = { input -> height = input.filter { it.isDigit() } },
                label = { Text("Altura (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = temperament,
                onValueChange = { temperament = it },
                label = { Text("Temperamento") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ADN verificado por veterinario")
                Switch(checked = isDnaVerified, onCheckedChange = { isDnaVerified = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Visible en el catálogo público")
                Switch(checked = isPublicCatalog, onCheckedChange = { isPublicCatalog = it })
            }

            Button(
                onClick = {
                    viewModel.registerHorse(
                        name = name,
                        registrationNumber = registrationNumber,
                        sex = sex,
                        height = height.toIntOrNull() ?: 0,
                        temperament = temperament,
                        isDnaVerified = isDnaVerified,
                        isPublicCatalog = isPublicCatalog
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar en mi criadero")
                }
            }
        }
    }
}
