package com.example.e_faktura.ui.invoice.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.model.InvoiceStatus
import com.example.e_faktura.model.getStatus
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.navigation.Screen

@Composable
fun InvoiceDashboardScreen(
    navController: NavController,
    invoiceViewModel: InvoiceListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val invoices = uiState.invoices

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                RevenueCard(
                    invoices = invoices,
                    onDetailsClick = { navController.navigate(Screen.Statistics.route) }
                )
            }

            item {
                Text(
                    text = "Twoje Faktury",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (invoices.isEmpty()) {
                item { EmptyState() }
            } else {
                items(invoices, key = { it.id }) { invoice ->
                    InvoiceItem(
                        invoice = invoice,
                        onClick = {
                            navController.navigate("invoice_details/${invoice.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RevenueCard(invoices: List<Invoice>, onDetailsClick: () -> Unit) {
    var balanceVisible by rememberSaveable { mutableStateOf(false) }

    val totalRevenue = invoices.filter {
        (it.type == "PRZYCHOD" || it.type == "") && it.isPaid
    }.sumOf { it.amount }

    val balanceText = if (balanceVisible) String.format("%,.2f", totalRevenue) else "•••••"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
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
                .padding(24.dp)
        ) {
            Text(
                text = "Zrealizowany Przychód (Opłacone)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
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
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.clickable { onDetailsClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zobacz statystyki",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp).padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceItem(invoice: Invoice, onClick: () -> Unit) {
    val status = invoice.getStatus()
    val isRevenue = invoice.type == "PRZYCHOD" || invoice.type == ""

    val (statusText, statusColor) = when(status) {
        InvoiceStatus.PAID -> "Zapłacono" to Color(0xFF4CAF50)
        InvoiceStatus.PENDING -> "Oczekuje" to MaterialTheme.colorScheme.primary
        InvoiceStatus.OVERDUE -> "PRZEDAWNIONA" to MaterialTheme.colorScheme.error
    }

    val typeColor = if (isRevenue) MaterialTheme.colorScheme.primary else Color(0xFFE91E63)
    val typeIcon = if (isRevenue) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = typeIcon, contentDescription = null, tint = typeColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = invoice.buyerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${if (isRevenue) "" else "- "}${String.format("%,.2f", invoice.amount)} PLN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isRevenue) MaterialTheme.colorScheme.onSurfaceVariant else Color.Red
                )
                Text(text = statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = statusColor)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline)
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
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "Brak faktur do wyświetlenia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Dodaj swoją pierwszą fakturę używając przycisku (+) na dole ekranu.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}