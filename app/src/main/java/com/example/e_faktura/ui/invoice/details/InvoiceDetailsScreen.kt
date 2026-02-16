@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkManager
import com.example.e_faktura.model.InvoiceStatus
import com.example.e_faktura.model.KsefStatus
import com.example.e_faktura.model.getKsefStatus
import com.example.e_faktura.model.getStatus
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.utils.PdfInvoiceGenerator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvoiceDetailsScreen(
    invoiceId: String?,
    navController: NavController,
    viewModel: InvoiceDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error, uiState.ksefMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            viewModel.clearMessages()
        }
        uiState.ksefMessage?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(invoiceId) {
        if (invoiceId != null) viewModel.loadInvoice(invoiceId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń fakturę") },
            text = { Text("Czy na pewno chcesz usunąć tę fakturę?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteInvoice { navController.popBackStack() }
                }) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Usuń", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val invoice = uiState.invoice
        if (invoice != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── Karta kwoty ───────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val displayAmount = if (invoice.grossAmount > 0) invoice.grossAmount else invoice.amount
                        Text(
                            "${String.format("%.2f", displayAmount)} PLN",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (invoice.netAmount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "netto: ${String.format("%.2f", invoice.netAmount)} PLN  " +
                                "VAT ${invoice.vatRate}%: ${String.format("%.2f", invoice.vatAmount)} PLN",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        val status = invoice.getStatus()
                        val (statusText, statusColor) = when (status) {
                            InvoiceStatus.PAID    -> "Opłacona"          to Color(0xFF4CAF50)
                            InvoiceStatus.PENDING -> "Oczekuje"          to MaterialTheme.colorScheme.primary
                            InvoiceStatus.OVERDUE -> "PRZETERMINOWANA"   to MaterialTheme.colorScheme.error
                        }
                        Text(statusText, color = statusColor, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.togglePaidStatus() }) {
                            Icon(if (invoice.isPaid) Icons.Default.Close else Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (invoice.isPaid) "OZNACZ JAKO NIEOPŁACONĄ" else "OZNACZ JAKO OPŁACONĄ")
                        }
                    }
                }

                // ─── Dane faktury ──────────────────────────────────────────
                InfoSection(title = "DANE FAKTURY") {
                    DetailRow(Icons.Default.Numbers, "Numer", invoice.invoiceNumber)
                    DetailRow(
                        Icons.Default.Event, "Data wystawienia",
                        if (invoice.invoiceDate > 0)
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(invoice.invoiceDate))
                        else "—"
                    )
                    DetailRow(
                        Icons.Default.EventBusy, "Termin płatności",
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(invoice.dueDate))
                    )
                    if (invoice.serviceDescription.isNotBlank()) {
                        DetailRow(Icons.Default.Description, "Opis", invoice.serviceDescription)
                    }
                    DetailRow(Icons.Default.Payment, "Forma płatności", invoice.paymentMethod)
                }

                // ─── Nabywca ───────────────────────────────────────────────
                InfoSection(title = "NABYWCA") {
                    DetailRow(Icons.Default.Business, "Nazwa", invoice.buyerName)
                    DetailRow(Icons.Default.Badge, "NIP", invoice.buyerNip.ifBlank { "—" })
                }

                // ─── KSeF ──────────────────────────────────────────────────
                KsefSection(
                    invoice = invoice,
                    isSending = uiState.isSendingToKsef,
                    onSendClick = { viewModel.sendToKsef(workManager) }
                )

                // ─── PDF ───────────────────────────────────────────────────
                Button(
                    onClick = { PdfInvoiceGenerator.generateAndOpenPdf(context, invoice) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(8.dp))
                    Text("POBIERZ PDF", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))
            }
        } else if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun KsefSection(
    invoice: com.example.e_faktura.model.Invoice,
    isSending: Boolean,
    onSendClick: () -> Unit
) {
    val ksefStatus = invoice.getKsefStatus()
    val (statusText, statusColor, statusIcon) = when (ksefStatus) {
        KsefStatus.LOCAL    -> Triple("Nie wysłano do KSeF",        Color(0xFF9E9E9E),                       Icons.Default.CloudOff)
        KsefStatus.SENDING  -> Triple("Wysyłanie...",               MaterialTheme.colorScheme.primary,        Icons.Default.CloudUpload)
        KsefStatus.SENT     -> Triple("Oczekuje na weryfikację",    Color(0xFFFF9800),                       Icons.Default.HourglassEmpty)
        KsefStatus.ACCEPTED -> Triple("Zaakceptowana przez KSeF ✓", Color(0xFF4CAF50),                      Icons.Default.CloudDone)
        KsefStatus.REJECTED -> Triple("Odrzucona przez KSeF ✗",    MaterialTheme.colorScheme.error,         Icons.Default.CloudOff)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("KSEF", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(statusText, style = MaterialTheme.typography.bodyMedium,
                    color = statusColor, fontWeight = FontWeight.SemiBold)
            }
            if (invoice.ksefReferenceNumber.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Nr ref: ${invoice.ksefReferenceNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (ksefStatus == KsefStatus.LOCAL || ksefStatus == KsefStatus.REJECTED) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSendClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (ksefStatus == KsefStatus.REJECTED) "WYŚLIJ PONOWNIE" else "WYŚLIJ DO KSEF",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}
