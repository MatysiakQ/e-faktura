package com.example.e_faktura.ui.invoice.add

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.invoice.InvoiceViewModel
import java.util.UUID

@Composable
fun AddInvoiceScreen(
    navController: NavController,
    onInvoiceAdded: () -> Unit,
    viewModel: InvoiceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var buyerNip by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val companyFromGus by viewModel.gusSearchResult.collectAsState()
    val isLoadingGus by viewModel.isLoadingGus.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    savedStateHandle?.get<String>("scannedNip")?.let {
        buyerNip = it
        savedStateHandle.remove<String>("scannedNip")
    }

    LaunchedEffect(companyFromGus) {
        companyFromGus?.let { buyerName = it.businessName }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dodaj nową fakturę", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = buyerNip,
            onValueChange = { buyerNip = it },
            label = { Text("NIP Nabywcy") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigate("scan_qr") }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Skanuj kod QR")
                    }
                    if (isLoadingGus) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        IconButton(onClick = { viewModel.fetchGusData(buyerNip) }) {
                            Icon(Icons.Default.Search, contentDescription = "Szukaj w GUS")
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = buyerName, onValueChange = { buyerName = it }, label = { Text("Nazwa Nabywcy") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Kwota (PLN)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            val newInvoice = Invoice(
                id = UUID.randomUUID().toString(),
                buyerNip = buyerNip,
                buyerName = buyerName,
                amount = amount.toDoubleOrNull() ?: 0.0,
                date = System.currentTimeMillis(),
                isPaid = false
            )
            viewModel.addInvoice(newInvoice)
            onInvoiceAdded()
        }) {
            Text("Zapisz fakturę")
        }
    }
}
