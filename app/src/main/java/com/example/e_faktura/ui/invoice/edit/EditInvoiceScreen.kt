@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.e_faktura.ui.invoice.edit

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.invoice.add.PAYMENT_METHODS
import com.example.e_faktura.ui.invoice.add.VAT_RATES
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditInvoiceScreen(
    navController: NavController,
    viewModel: EditInvoiceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var expandedVat     by remember { mutableStateOf(false) }
    var expandedPayment by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                EditInvoiceEvent.SaveSuccess -> {
                    Toast.makeText(context, "Faktura zaktualizowana!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is EditInvoiceEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edytuj fakturę", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.notFound -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("Nie znaleziono faktury", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // ─── Typ dokumentu ───────────────────────────────────────
                    Text("Typ dokumentu", style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = state.type == "PRZYCHOD",
                            onClick = { viewModel.updateType("PRZYCHOD") },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text("Przychód") }
                        SegmentedButton(
                            selected = state.type == "KOSZT",
                            onClick = { viewModel.updateType("KOSZT") },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text("Koszt") }
                    }

                    // ─── Numer faktury ───────────────────────────────────────
                    OutlinedTextField(
                        value = state.invoiceNumber,
                        onValueChange = { viewModel.updateNumber(it) },
                        label = { Text("Numer faktury") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Numbers, null) },
                        isError = state.error?.contains("numer", true) == true,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // ─── Nabywca ──────────────────────────────────────────────
                    OutlinedTextField(
                        value = state.buyerName,
                        onValueChange = { viewModel.updateBuyerName(it) },
                        label = { Text(if (state.type == "PRZYCHOD") "Nabywca" else "Sprzedawca") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Business, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.buyerNip,
                        onValueChange = { viewModel.updateBuyerNip(it) },
                        label = { Text("NIP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Tag, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // ─── Kwota netto ──────────────────────────────────────────
                    OutlinedTextField(
                        value = state.netAmountInput,
                        onValueChange = { viewModel.updateNetAmount(it) },
                        label = { Text("Kwota netto (PLN)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        isError = state.error?.contains("kwotę", true) == true,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // ─── Stawka VAT ───────────────────────────────────────────
                    Text("Stawka VAT", style = MaterialTheme.typography.labelLarge)
                    ExposedDropdownMenuBox(
                        expanded = expandedVat,
                        onExpandedChange = { expandedVat = it }
                    ) {
                        OutlinedTextField(
                            value = if (state.vatRate == "ZW") "Zwolniony" else "${state.vatRate}%",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedVat) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedVat,
                            onDismissRequest = { expandedVat = false }
                        ) {
                            VAT_RATES.forEach { rate ->
                                DropdownMenuItem(
                                    text = { Text(if (rate == "ZW") "Zwolniony" else "$rate%") },
                                    onClick = { viewModel.updateVatRate(rate); expandedVat = false }
                                )
                            }
                        }
                    }

                    // ─── Podsumowanie VAT ─────────────────────────────────────
                    if (state.grossAmount > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Netto", style = MaterialTheme.typography.labelSmall)
                                    val net = state.netAmountInput.toDoubleOrNull() ?: 0.0
                                    Text("${String.format("%.2f", net)} PLN", fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("VAT", style = MaterialTheme.typography.labelSmall)
                                    Text("${String.format("%.2f", state.vatAmount)} PLN", fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Brutto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "${String.format("%.2f", state.grossAmount)} PLN",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // ─── Opis usługi ──────────────────────────────────────────
                    OutlinedTextField(
                        value = state.serviceDescription,
                        onValueChange = { viewModel.updateServiceDescription(it) },
                        label = { Text("Opis usługi / towaru") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // ─── Forma płatności ──────────────────────────────────────
                    Text("Forma płatności", style = MaterialTheme.typography.labelLarge)
                    ExposedDropdownMenuBox(
                        expanded = expandedPayment,
                        onExpandedChange = { expandedPayment = it }
                    ) {
                        OutlinedTextField(
                            value = state.paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPayment) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPayment,
                            onDismissRequest = { expandedPayment = false }
                        ) {
                            PAYMENT_METHODS.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = { viewModel.updatePaymentMethod(method); expandedPayment = false }
                                )
                            }
                        }
                    }

                    // ─── Błąd ─────────────────────────────────────────────────
                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.saveChanges() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("ZAPISZ ZMIANY", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}
