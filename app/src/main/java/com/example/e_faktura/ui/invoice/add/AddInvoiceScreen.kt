package com.example.e_faktura.ui.invoice.add

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(
    navController: NavController,
    onInvoiceAdded: () -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val isLoadingGus by invoiceViewModel.isLoadingGus.collectAsState()
    val nextInvoiceNumber by invoiceViewModel.nextInvoiceNumber.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wystaw Fakturę", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { 
                    invoiceViewModel.issueInvoice {
                        Toast.makeText(context, "Faktura wystawiona", Toast.LENGTH_SHORT).show()
                        onInvoiceAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoadingGus,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("WYSTAW FAKTURĘ", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Visual Anchor ---
            VisualIdentitySection()

            // --- Invoice Identity ---
            InvoiceIdentitySection(nextInvoiceNumber = nextInvoiceNumber)

            // --- Client Details ---
            ClientDetailsSection(
                nip = uiState.nipToSearch,
                onNipChange = { invoiceViewModel.onNipToSearchChange(it) },
                onSearchClick = { invoiceViewModel.fetchGusData(uiState.nipToSearch) },
                isLoading = isLoadingGus,
                clientName = uiState.buyerName,
                onClientNameChange = { invoiceViewModel.onBuyerNameChange(it) }
            )

            // --- Financial Details ---
            FinancialDetailsSection(uiState = uiState, viewModel = invoiceViewModel)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VisualIdentitySection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PostAdd,
                contentDescription = "Ikona faktury",
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text("Nowa Faktura Sprzedaży", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InvoiceIdentitySection(nextInvoiceNumber: String) {
    OutlinedTextField(
        value = nextInvoiceNumber,
        onValueChange = {}, // Read-only
        label = { Text("Numer Faktury (Automatyczny)") },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
        readOnly = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun ClientDetailsSection(
    nip: String, onNipChange: (String) -> Unit, 
    onSearchClick: () -> Unit, 
    isLoading: Boolean, 
    clientName: String, 
    onClientNameChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Dane Klienta (Nabywca)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = nip,
            onValueChange = onNipChange,
            label = { Text("NIP Klienta") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = onSearchClick) {
                        Text("SZUKAJ")
                    }
                }
            },
            singleLine = true
        )
        OutlinedTextField(
            value = clientName,
            onValueChange = onClientNameChange,
            label = { Text("Nazwa Klienta") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun FinancialDetailsSection(uiState: AddInvoiceUiState, viewModel: InvoiceViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val issueDateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        calendar.set(year, month, dayOfMonth)
        viewModel.onIssueDateChange(calendar.timeInMillis)
    }
    val paymentDateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        calendar.set(year, month, dayOfMonth)
        viewModel.onPaymentDueDateChange(calendar.timeInMillis)
    }

    val formattedIssueDate = remember(uiState.issueDate) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(uiState.issueDate))
    }
    val formattedPaymentDueDate = remember(uiState.paymentDueDate) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(uiState.paymentDueDate))
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Szczegóły Finansowe", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = formattedIssueDate,
            onValueChange = {},
            label = { Text("Data wystawienia") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { 
                DatePickerDialog(
                    context,
                    issueDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = formattedPaymentDueDate,
            onValueChange = {},
            label = { Text("Termin płatności") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { 
                DatePickerDialog(
                    context,
                    paymentDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text("Kwota (Netto)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
        )
    }
}
