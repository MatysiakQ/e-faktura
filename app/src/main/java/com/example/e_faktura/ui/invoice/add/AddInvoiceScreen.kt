package com.example.e_faktura.ui.invoice.add

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel(),
    onInvoiceAdded: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Obsługa sukcesu zapisu
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            Toast.makeText(context, "Faktura zapisana!", Toast.LENGTH_SHORT).show()
            onInvoiceAdded()
            viewModel.resetState() // Reset stanu po wyjściu
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowa Faktura") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sekcja: Dane podstawowe
                Text("Dane dokumentu", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = state.invoiceNumber,
                    onValueChange = { viewModel.updateInvoiceNumber(it) },
                    label = { Text("Numer faktury") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Wybór Typu (Sprzedaż / Zakup)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Typ:", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = state.type == "SALE",
                        onClick = { viewModel.updateType(true) }
                    )
                    Text("Sprzedaż", modifier = Modifier.clickable { viewModel.updateType(true) })
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = state.type == "PURCHASE",
                        onClick = { viewModel.updateType(false) }
                    )
                    Text("Zakup", modifier = Modifier.clickable { viewModel.updateType(false) })
                }

                DateSelector(
                    label = "Data wystawienia",
                    dateMillis = state.date,
                    onDateSelected = { /* TODO: Dodaj updateDate w ViewModel jeśli potrzebne */ }
                )

                Divider()

                // Sekcja: Kontrahent
                Text("Kontrahent", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = state.buyerNip,
                    onValueChange = { viewModel.updateBuyerNip(it) },
                    label = { Text("NIP Nabywcy") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.buyerName,
                    onValueChange = { viewModel.updateBuyerName(it) },
                    label = { Text("Nazwa Nabywcy") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()

                // Sekcja: Finanse (Kluczowa zmiana)
                Text("Kwoty", style = MaterialTheme.typography.titleMedium)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.netValue,
                        onValueChange = { viewModel.updateNetValue(it) },
                        label = { Text("Netto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(0.6f)
                    )

                    OutlinedTextField(
                        value = state.vatRate,
                        onValueChange = { viewModel.updateVatRate(it) },
                        label = { Text("VAT %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(0.4f)
                    )
                }

                // Kalkulacja podglądowa w UI
                val net = state.netValue.toDoubleOrNull() ?: 0.0
                val vatRate = (state.vatRate.toDoubleOrNull() ?: 23.0) / 100.0
                val calculatedVat = (net * vatRate * 100.0).roundToInt() / 100.0
                val calculatedGross = ((net + calculatedVat) * 100.0).roundToInt() / 100.0

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = String.format("%.2f", calculatedVat),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kwota VAT") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format("%.2f", calculatedGross),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Brutto") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveInvoice() },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("ZAPISZ FAKTURĘ")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DateSelector(label: String, dateMillis: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateMillis

    val dateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
        calendar.set(year, month, day)
        onDateSelected(calendar.timeInMillis)
    }

    val formattedDate = remember(dateMillis) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
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
        enabled = false // Na razie disabled, dopóki nie dodasz updateDate w ViewModel
    )
}