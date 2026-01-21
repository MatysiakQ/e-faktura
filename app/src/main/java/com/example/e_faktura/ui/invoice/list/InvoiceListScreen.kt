package com.example.e_faktura.ui.invoice.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.e_faktura.model.Invoice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceListScreen(
    navController: NavController,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    // FIX: Collect the 'invoices' StateFlow directly, not a 'uiState' wrapper.
    val invoiceList by viewModel.invoices.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_invoice") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nową fakturę")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FIX: Iterate over 'invoiceList' and use the correct key 'it.id'.
            items(invoiceList, key = { it.id }) { invoice ->
                FullInvoiceListItem(invoice = invoice) {
                    // navController.navigate("invoice_details/${invoice.id}")
                }
            }
        }
    }
}

@Composable
private fun FullInvoiceListItem(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // FIX: Use the correct property names from the Invoice data class.
                Text(
                    text = invoice.invoiceNumber,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Nabywca: ${invoice.buyerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Data: ${formatDate(invoice.date)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f zł", invoice.grossValue),
                    fontWeight = FontWeight.Bold,
                    color = if (invoice.type == "SALE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (invoice.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (invoice.isPaid) "Opłacona" else "Do zapłaty",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (invoice.isPaid) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
