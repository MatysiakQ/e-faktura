package com.example.e_faktura.ui.invoice.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.ui.AppViewModelProvider
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
            RevenueCard(
                invoices = invoices,
                onDetailsClick = { navController.navigate(Screen.Statistics.route) }
            )
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
fun RevenueCard(invoices: List<Invoice>, onDetailsClick: () -> Unit) {
    var balanceVisible by rememberSaveable { mutableStateOf(false) } // Default to hidden
    val totalRevenue = invoices.sumOf { it.amount }
    val balanceText = if (balanceVisible) String.format("%,.2f", totalRevenue) else "•••••"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Przychody",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$balanceText PLN",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 32.sp
                )
                IconButton(onClick = { balanceVisible = !balanceVisible }) {
                    Icon(
                        imageVector = if (balanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Pokaż/Ukryj saldo",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.clickable(onClick = onDetailsClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zobacz szczegóły",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                )
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
            imageVector = Icons.Filled.ReceiptLong,
            contentDescription = "Brak faktur",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
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
