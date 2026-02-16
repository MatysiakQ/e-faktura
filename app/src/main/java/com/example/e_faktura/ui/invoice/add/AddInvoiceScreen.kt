package com.example.e_faktura.ui.invoice.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(
    navController: NavController,
    onInvoiceAdded: () -> Unit,
    viewModel: InvoiceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val companies by viewModel.savedCompanies.collectAsState()

    var expandedCompany by remember { mutableStateOf(false) }
    var expandedVat by remember { mutableStateOf(false) }
    var expandedPayment by remember { mutableStateOf(false) }

    val isRevenue = state.type == "PRZYCHOD"
    val partyLabel = if (isRevenue) "Nabywca" else "Sprzedawca"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nowa faktura",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // ─── Typ dokumentu ────────────────────────────────────────────────────
        Text("Typ dokumentu", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = isRevenue,
                onClick = { viewModel.updateType("PRZYCHOD") },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text("Przychód") }
            SegmentedButton(
                selected = !isRevenue,
                onClick = { viewModel.updateType("KOSZT") },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text("Koszt") }
        }

        // ─── Numer faktury ────────────────────────────────────────────────────
        OutlinedTextField(
            value = state.invoiceNumber,
            onValueChange = { viewModel.updateNumber(it) },
            label = { Text("Numer faktury") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Numbers, null) },
            placeholder = { Text("np. FV/2026/01/001") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = state.error?.contains("numer", true) == true
        )

        // ─── Import z zapisanych firm ─────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedCompany = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Business, null)
                Spacer(Modifier.width(8.dp))
                Text("IMPORTUJ DANE ($partyLabel)")
            }
            DropdownMenu(expanded = expandedCompany, onDismissRequest = { expandedCompany = false }) {
                if (companies.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Brak zapisanych firm") },
                        onClick = { expandedCompany = false }
                    )
                } else {
                    companies.forEach { company ->
                        DropdownMenuItem(
                            text = { Text("${company.displayName} (NIP: ${company.nip})") },
                            onClick = { viewModel.selectCompany(company); expandedCompany = false }
                        )
                    }
                }
            }
        }

        // ─── NIP + wyszukiwanie GUS ───────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.buyerNip,
                onValueChange = viewModel::updateBuyerNip,
                label = { Text("NIP $partyLabel") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { viewModel.fetchCompanyFromGus() },
                enabled = !state.isLoadingGus,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoadingGus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Search, "Szukaj w GUS")
                }
            }
        }

        // ─── Nazwa nabywcy/sprzedawcy ─────────────────────────────────────────
        OutlinedTextField(
            value = state.buyerName,
            onValueChange = viewModel::updateBuyerName,
            label = { Text("Nazwa $partyLabel") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // ─── Opis usługi / towaru ────────────────────────────────────────────
        OutlinedTextField(
            value = state.serviceDescription,
            onValueChange = viewModel::updateServiceDescription,
            label = { Text("Opis usługi / towaru") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 3
        )

        HorizontalDivider()
        Text("Kwoty", style = MaterialTheme.typography.labelLarge)

        // ─── Stawka VAT ───────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Stawka VAT:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(100.dp)
            )
            Box {
                OutlinedButton(
                    onClick = { expandedVat = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (state.vatRate == "ZW") "Zwolniony" else "${state.vatRate}%")
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = expandedVat, onDismissRequest = { expandedVat = false }) {
                    VAT_RATES.forEach { rate ->
                        DropdownMenuItem(
                            text = { Text(if (rate == "ZW") "ZW – Zwolniony" else "$rate%") },
                            onClick = { viewModel.updateVatRate(rate); expandedVat = false }
                        )
                    }
                }
            }
        }

        // ─── Kwota netto ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = state.netAmountInput,
            onValueChange = viewModel::updateNetAmount,
            label = { Text("Kwota netto (PLN)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = state.error?.contains("kwotę", true) == true,
            supportingText = if (state.error?.contains("kwotę", true) == true) {
                { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            } else null
        )

        // ─── Podsumowanie kwot (auto-wyliczone) ───────────────────────────────
        if ((state.netAmountInput.toDoubleOrNull() ?: 0.0) > 0.0) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AmountRow("Netto:", "${String.format("%.2f", state.netAmountInput.toDoubleOrNull() ?: 0.0)} PLN")
                    AmountRow("VAT ${if (state.vatRate == "ZW") "ZW" else "${state.vatRate}%"}:", "${String.format("%.2f", state.vatAmount)} PLN")
                    HorizontalDivider(thickness = 0.5.dp)
                    AmountRow(
                        label = "Brutto:",
                        value = "${String.format("%.2f", state.grossAmount)} PLN",
                        bold = true
                    )
                }
            }
        }

        // ─── Forma płatności ──────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Płatność:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(100.dp)
            )
            Box {
                OutlinedButton(
                    onClick = { expandedPayment = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(state.paymentMethod)
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = expandedPayment, onDismissRequest = { expandedPayment = false }) {
                    PAYMENT_METHODS.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = { viewModel.updatePaymentMethod(method); expandedPayment = false }
                        )
                    }
                }
            }
        }

        // ─── Komunikat błędu ogólny ───────────────────────────────────────────
        if (state.error != null && !state.error!!.contains("kwotę", true) && !state.error!!.contains("numer", true)) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // ─── Przycisk zapisu ──────────────────────────────────────────────────
        Button(
            onClick = { viewModel.saveInvoice(onInvoiceAdded) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("ZAPISZ FAKTURĘ", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AmountRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
