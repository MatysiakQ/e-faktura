package com.example.e_faktura.ui.invoice.add

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddInvoiceScreen(
    navController: NavController,
    onInvoiceAdded: () -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val isLoadingGus by invoiceViewModel.isLoadingGus.collectAsState()

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            GusSearchSection(invoiceViewModel, uiState, isLoadingGus)

            Spacer(modifier = Modifier.height(16.dp))

            InvoiceDetailsSection(invoiceViewModel, uiState)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { invoiceViewModel.addInvoice(onInvoiceAdded) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoadingGus
            ) {
                Text("Zapisz fakturę")
            }
        }
    }
}

@Composable
private fun GusSearchSection(invoiceViewModel: InvoiceViewModel, uiState: AddInvoiceUiState, isLoading: Boolean) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pobierz dane z GUS", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.nipToSearch,
                    onValueChange = { invoiceViewModel.onNipToSearchChange(it) },
                    label = { Text("NIP") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { invoiceViewModel.fetchGusData(uiState.nipToSearch) }, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    }
                }
            }
        }
    }
}


@Composable
private fun InvoiceDetailsSection(invoiceViewModel: InvoiceViewModel, uiState: AddInvoiceUiState) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        calendar.set(year, month, dayOfMonth)
        invoiceViewModel.onDateChange(calendar.timeInMillis)
    }

    val formattedDate = remember(uiState.issueDate) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(uiState.issueDate))
    }

    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Dane faktury", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.buyerName,
                onValueChange = { invoiceViewModel.onBuyerNameChange(it) },
                label = { Text("Nabywca") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) }
            )

            OutlinedTextField(
                value = uiState.buyerNip,
                onValueChange = { invoiceViewModel.onBuyerNipChange(it) },
                label = { Text("NIP Nabywcy") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = uiState.buyerAddress,
                onValueChange = { invoiceViewModel.onBuyerAddressChange(it) },
                label = { Text("Adres") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formattedDate,
                onValueChange = {}, // Not editable
                label = { Text("Data wystawienia") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            dateSetListener,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { invoiceViewModel.onAmountChange(it) },
                label = { Text("Kwota") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Money, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}
