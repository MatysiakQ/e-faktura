package com.example.e_faktura.ui.invoice.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.model.InvoiceStatus
import com.example.e_faktura.model.getStatus
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDashboardScreen(
    navController: NavController,
    invoiceViewModel: InvoiceListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    var searchActive by rememberSaveable { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ─── Karta przychodu ─────────────────────────────────────────────────
        item {
            RevenueCard(
                invoices = uiState.allInvoices,
                onDetailsClick = { navController.navigate(Screen.Statistics.route) }
            )
        }

        // ─── SearchBar ───────────────────────────────────────────────────────
        item {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { invoiceViewModel.updateSearch(it) },
                active = searchActive,
                onActiveChange = { searchActive = it },
                placeholder = { Text("Szukaj: nabywca, NIP, numer...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty() || searchActive) {
                        IconButton(onClick = {
                            invoiceViewModel.updateSearch("")
                            searchActive = false
                        }) { Icon(Icons.Default.Close, null) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Podpowiedzi podczas wpisywania
                uiState.filteredInvoices.take(5).forEach { inv ->
                    ListItem(
                        headlineContent = { Text(inv.buyerName) },
                        supportingContent = { Text(inv.invoiceNumber) },
                        leadingContent = { Icon(Icons.Default.Receipt, null) },
                        modifier = Modifier.clickable {
                            invoiceViewModel.updateSearch(inv.buyerName)
                            searchActive = false
                        }
                    )
                }
            }
        }

        // ─── Filtry ──────────────────────────────────────────────────────────
        item {
            FilterChipsRow(
                activeFilter = uiState.activeFilter,
                onFilterSelected = { invoiceViewModel.setFilter(it) },
                invoices = uiState.allInvoices
            )
        }

        // ─── Nagłówek listy ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.searchQuery.isBlank() && uiState.activeFilter == InvoiceFilter.ALL)
                        "Wszystkie faktury" else "Wyniki (${uiState.filteredInvoices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AnimatedVisibility(visible = uiState.searchQuery.isNotBlank() || uiState.activeFilter != InvoiceFilter.ALL) {
                    TextButton(onClick = {
                        invoiceViewModel.updateSearch("")
                        invoiceViewModel.setFilter(InvoiceFilter.ALL)
                    }) { Text("Wyczyść") }
                }
            }
        }

        // ─── Lista faktur ────────────────────────────────────────────────────
        if (uiState.filteredInvoices.isEmpty()) {
            item {
                EmptyState(
                    isFiltered = uiState.searchQuery.isNotBlank() || uiState.activeFilter != InvoiceFilter.ALL
                )
            }
        } else {
            items(uiState.filteredInvoices, key = { it.id }) { invoice ->
                InvoiceItem(
                    invoice = invoice,
                    onClick = { navController.navigate("invoice_details/${invoice.id}") }
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    activeFilter: InvoiceFilter,
    onFilterSelected: (InvoiceFilter) -> Unit,
    invoices: List<Invoice>
) {
    val now = System.currentTimeMillis()

    data class FilterDef(
        val filter: InvoiceFilter,
        val label: String,
        val count: Int
    )

    val filters = listOf(
        FilterDef(InvoiceFilter.ALL, "Wszystkie", invoices.size),
        FilterDef(InvoiceFilter.REVENUE, "Przychody", invoices.count { it.type == "PRZYCHOD" }),
        FilterDef(InvoiceFilter.COST, "Koszty", invoices.count { it.type == "KOSZT" }),
        FilterDef(InvoiceFilter.UNPAID, "Do opłacenia", invoices.count { it.type == "PRZYCHOD" && !it.isPaid }),
        FilterDef(InvoiceFilter.OVERDUE, "Przeterminowane", invoices.count { !it.isPaid && it.dueDate > 0 && it.dueDate < now })
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        items(filters) { f ->
            FilterChip(
                selected = activeFilter == f.filter,
                onClick = { onFilterSelected(f.filter) },
                label = {
                    Text("${f.label} (${f.count})")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (f.filter == InvoiceFilter.OVERDUE)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun RevenueCard(invoices: List<Invoice>, onDetailsClick: () -> Unit) {
    var balanceVisible by rememberSaveable { mutableStateOf(false) }

    val totalRevenue = invoices.filter { it.type == "PRZYCHOD" && it.isPaid }
        .sumOf { if (it.grossAmount > 0) it.grossAmount else it.amount }

    val balanceText = if (balanceVisible) String.format("%,.2f", totalRevenue) else "•••••"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                "Zrealizowany przychód (opłacone)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "$balanceText PLN",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 32.sp
                )
                IconButton(onClick = { balanceVisible = !balanceVisible }) {
                    Icon(
                        if (balanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
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
                        "Zobacz statystyki",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceItem(invoice: Invoice, onClick: () -> Unit) {
    val status = invoice.getStatus()
    val isRevenue = invoice.type == "PRZYCHOD" || invoice.type == ""

    val (statusText, statusColor) = when (status) {
        InvoiceStatus.PAID    -> "Zapłacono" to Color(0xFF4CAF50)
        InvoiceStatus.PENDING -> "Oczekuje"  to MaterialTheme.colorScheme.primary
        InvoiceStatus.OVERDUE -> "PRZETERMINOWANA" to MaterialTheme.colorScheme.error
    }

    val typeColor = if (isRevenue) MaterialTheme.colorScheme.primary else Color(0xFFE91E63)
    val typeIcon  = if (isRevenue) Icons.Default.TrendingUp else Icons.Default.TrendingDown

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
                Icon(typeIcon, null, tint = typeColor)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invoice.buyerName.ifBlank { "Brak nabywcy" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${if (isRevenue) "" else "- "}${
                        String.format("%,.2f", if (invoice.grossAmount > 0) invoice.grossAmount else invoice.amount)
                    } PLN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isRevenue) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFE91E63)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = statusColor)
                    if (invoice.invoiceNumber.isNotBlank()) {
                        Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(invoice.invoiceNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun EmptyState(isFiltered: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            if (isFiltered) Icons.Default.SearchOff else Icons.Default.ReceiptLong,
            null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            if (isFiltered) "Brak wyników" else "Brak faktur",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (isFiltered) "Zmień kryteria wyszukiwania lub wyczyść filtry."
            else "Dodaj swoją pierwszą fakturę używając przycisku (+) na dole ekranu.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
