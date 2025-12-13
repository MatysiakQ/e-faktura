package com.example.e_faktura.ui.invoice.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.components.FinancialDashboardCard
import com.example.e_faktura.ui.navigation.Screen

@Composable
fun InvoiceDashboardScreen(
    navController: NavController,
    invoiceViewModel: InvoiceListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val invoices = uiState.invoices

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FinancialDashboardCard(onCardClick = { navController.navigate(Screen.Statistics.route) })
        }

        item {
            Text(
                text = "Ostatnie faktury",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (invoices.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            items(invoices, key = { it.id }) { invoice ->
                InvoiceItem(invoice = invoice)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = "Brak faktur",
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        Text(
            text = "Brak faktur do wyświetlenia",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Dodaj swoją pierwszą fakturę używając przycisku (+) na dole ekranu.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InvoiceItem(invoice: Invoice) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Faktura dla: ${invoice.buyerName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kwota: ${String.format("%,.2f", invoice.amount)} PLN",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (invoice.isPaid) "Status: Zapłacono" else "Status: Oczekuje na płatność",
                color = if (invoice.isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
