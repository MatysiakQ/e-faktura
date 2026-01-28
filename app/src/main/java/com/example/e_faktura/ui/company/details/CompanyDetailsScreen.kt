package com.example.e_faktura.ui.company.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.utils.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(
    navController: NavController,
    detailsViewModel: CompanyDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by detailsViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog potwierdzenia usunięcia
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń firmę") },
            text = { Text("Czy na pewno chcesz usunąć tę firmę? Tej operacji nie da się cofnąć.") },
            confirmButton = {
                TextButton(onClick = {
                    detailsViewModel.deleteCompany { navController.popBackStack() }
                }) {
                    Text("Usuń", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Szczegóły firmy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) { CircularProgressIndicator() }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Text("Błąd: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.company != null -> {
                val company = uiState.company!!
                val qrBitmap = remember(company.nip) { QrCodeGenerator.generateQrBitmap(company.nip) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Główna karta z danymi firmy
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                // ✅ POPRAWKA: Użycie displayName (String) zamiast name (String?)
                                Text(
                                    text = company.displayName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                            DetailRow(icon = Icons.Default.Tag, label = "NIP", value = company.nip)
                            Spacer(Modifier.height(16.dp))

                            // ✅ POPRAWKA: Zabezpieczenie przed pustymi polami adresu
                            val addressLine = company.address.ifBlank { "Brak adresu" }
                            val cityLine = "${company.postalCode} ${company.city}".trim()

                            DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Adres",
                                value = if (cityLine.isEmpty()) addressLine else "$addressLine\n$cityLine"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Kod QR do faktur", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code w ładnej ramce
                    qrBitmap?.let {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Image(
                                bitmap = it,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(220.dp).padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}