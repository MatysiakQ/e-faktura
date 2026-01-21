package com.example.e_faktura.ui.invoice.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Numbers // ✅ DODANO
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Nowa faktura",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // ✅ DODANO: Pole Numer Faktury (Tego brakowało!)
        OutlinedTextField(
            value = state.invoiceNumber,
            onValueChange = { viewModel.updateNumber(it) },
            label = { Text("Numer faktury") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Numbers, null) },
            placeholder = { Text("np. FV/2026/01/01") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = state.error?.contains("Numer") == true
        )

        Spacer(Modifier.height(16.dp))

        // --- Import z Twoich zapisanych firm ---
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Business, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("IMPORTUJ Z TWOICH FIRM")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (companies.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Brak zapisanych firm") },
                        onClick = { expanded = false }
                    )
                } else {
                    companies.forEach { company ->
                        DropdownMenuItem(
                            text = { Text("${company.name} (NIP: ${company.nip})") },
                            onClick = {
                                viewModel.selectCompany(company)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Sekcja NIP + Wyszukiwanie w GUS ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.buyerNip,
                onValueChange = viewModel::updateBuyerNip,
                label = { Text("NIP Nabywcy") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
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
                    Icon(Icons.Default.Search, contentDescription = "Szukaj w GUS")
                }
            }
        }

        // Komunikat o błędzie
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Pozostałe pola ---
        OutlinedTextField(
            value = state.buyerName,
            onValueChange = viewModel::updateBuyerName,
            label = { Text("Nazwa Nabywcy") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.amount,
            onValueChange = viewModel::updateAmount,
            label = { Text("Kwota PLN") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(32.dp))

        // --- Przycisk zapisu ---
        Button(
            onClick = { viewModel.saveInvoice(onInvoiceAdded) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("ZAPISZ FAKTURĘ", fontWeight = FontWeight.Bold)
            }
        }
    }
}