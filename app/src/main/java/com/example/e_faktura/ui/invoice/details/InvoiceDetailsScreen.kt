package com.example.e_faktura.ui.invoice.details

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.utils.PdfInvoiceGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    invoiceId: String?,
    navController: NavController,
    // ✅ Korzystamy z naszej ręcznej fabryki bez Hilta
    viewModel: InvoiceDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ Inicjalizacja ładowania faktury po wejściu na ekran
    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            viewModel.loadInvoice(invoiceId)
        }
    }

    // Dialog potwierdzenia usunięcia
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń fakturę") },
            text = { Text("Czy na pewno chcesz trwale usunąć tę fakturę?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteInvoice { navController.popBackStack() }
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
                title = { Text("Szczegóły faktury", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    text = "Błąd: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                uiState.invoice != null -> {
                    val invoice = uiState.invoice!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // KARTA PODSUMOWANIA PŁATNOŚCI
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (invoice.isPaid) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "KWOTA DO ZAPŁATY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%,.2f", invoice.amount)} PLN",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.togglePaidStatus() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (invoice.isPaid) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(if (invoice.isPaid) Icons.Default.Close else Icons.Default.Check, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (invoice.isPaid) "OZNACZ JAKO NIEOPŁACONĄ" else "OZNACZ JAKO OPŁACONĄ")
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // SEKCJA: DANE FAKTURY
                        InfoSection(title = "DANE FAKTURY") {
                            // ✅ Używamy invoiceNumber zamiast number
                            DetailRow(Icons.Default.Numbers, "Numer", invoice.invoiceNumber)
                            // ✅ Używamy dueDate dla daty płatności
                            DetailRow(Icons.Default.Event, "Termin płatności", formatDate(invoice.dueDate))
                        }

                        Spacer(Modifier.height(16.dp))

                        // SEKCJA: NABYWCA
                        InfoSection(title = "NABYWCA") {
                            DetailRow(Icons.Default.Business, "Nazwa", invoice.buyerName)
                            // ✅ buyerNip jest teraz dostępny w modelu
                            DetailRow(Icons.Default.Badge, "NIP", invoice.buyerNip)
                        }

                        Spacer(Modifier.height(32.dp))

                        // PRZYCISK GENEROWANIA PDF
                        Button(
                            onClick = {
                                // ✅ Wywołanie generatora PDF
                                PdfInvoiceGenerator.generateAndOpenPdf(context, invoice)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(Modifier.width(8.dp))
                            Text("GENERUJ I OTWÓRZ PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) { content() }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (value.isBlank()) "Brak danych" else value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))