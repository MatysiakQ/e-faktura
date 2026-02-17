@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.e_faktura.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import kotlin.math.max

@Composable
fun StatisticsScreen(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onOverdueClick: () -> Unit
) {
    val uiState by statisticsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statystyki", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ─── Nagłówek miesiąca ────────────────────────────────────────────
            item {
                Text(
                    "Wyniki za ${uiState.currentMonth}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // ─── Kafelki główne ───────────────────────────────────────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Przychody",
                        value = uiState.revenue,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = Color(0xFF4CAF50)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Koszty",
                        value = uiState.costs,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = Color(0xFFF44336)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Zysk netto",
                        value = uiState.profit,
                        icon = Icons.Default.AccountBalance,
                        color = if (uiState.profit >= 0) Color(0xFF2196F3) else Color(0xFFF44336)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Oczekujące",
                        value = uiState.pendingAmount,
                        icon = Icons.Default.HourglassEmpty,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // ─── Karta VAT ────────────────────────────────────────────────────
            item {
                VatCard(
                    vatOwed = uiState.vatOwed,
                    vatPaid = uiState.vatPaid,
                    vatBalance = uiState.vatBalance
                )
            }

            // ─── Wykres miesięczny ────────────────────────────────────────────
            if (uiState.monthlyData.isNotEmpty()) {
                item {
                    MonthlyChartCard(monthlyData = uiState.monthlyData)
                }
            }

            // ─── Podsumowanie faktur ──────────────────────────────────────────
            item {
                InvoiceSummaryCard(
                    total = uiState.totalInvoices,
                    paid = uiState.paidInvoices
                )
            }

            // ─── Alert przedawnionych ─────────────────────────────────────────
            if (uiState.overdueCount > 0) {
                item {
                    OverdueAlertCard(
                        count = uiState.overdueCount,
                        amount = uiState.overdueAmount,
                        onClick = onOverdueClick
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── Karta VAT ────────────────────────────────────────────────────────────────
@Composable
private fun VatCard(vatOwed: Double, vatPaid: Double, vatBalance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.secondary)
                Text("Rozliczenie VAT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                VatRow("VAT należny (od sprzedaży)", vatOwed, MaterialTheme.colorScheme.error)
                VatRow("VAT naliczony (od zakupów)", vatPaid, Color(0xFF4CAF50))
            }
            HorizontalDivider(thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Do zapłaty / nadpłata:", fontWeight = FontWeight.SemiBold)
                Text(
                    "${if (vatBalance >= 0) "" else "-"}${String.format("%.2f", kotlin.math.abs(vatBalance))} PLN",
                    fontWeight = FontWeight.Bold,
                    color = if (vatBalance >= 0) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun VatRow(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Text("${String.format("%.2f", value)} PLN", fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ─── Wykres miesięczny ─────────────────────────────────────────────────────────
@Composable
private fun MonthlyChartCard(monthlyData: List<MonthlyData>) {
    val revenueColor = Color(0xFF4CAF50)
    val costsColor   = Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Trend ostatnich 6 miesięcy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            // Legenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ChartLegend("Przychody", revenueColor)
                ChartLegend("Koszty", costsColor)
            }

            // Wykres
            val maxValue = monthlyData.maxOf { max(it.revenue, it.costs) }.let { if (it == 0.0) 1.0 else it }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (monthlyData.size * 2.5f + 1)
                    val gap = barWidth * 0.5f
                    val chartHeight = size.height - 24.dp.toPx()
                    val startX = gap

                    monthlyData.forEachIndexed { i, data ->
                        val groupX = startX + i * (barWidth * 2 + gap * 2)

                        // Słupek przychodu
                        val revH = (data.revenue / maxValue * chartHeight).toFloat()
                        if (revH > 0) {
                            drawRoundRect(
                                color = revenueColor,
                                topLeft = Offset(groupX, chartHeight - revH),
                                size = Size(barWidth, revH),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }

                        // Słupek kosztu
                        val cstH = (data.costs / maxValue * chartHeight).toFloat()
                        if (cstH > 0) {
                            drawRoundRect(
                                color = costsColor,
                                topLeft = Offset(groupX + barWidth + gap * 0.5f, chartHeight - cstH),
                                size = Size(barWidth, cstH),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }

                        // Linia bazowa
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f),
                            start = Offset(groupX - gap * 0.5f, chartHeight),
                            end = Offset(groupX + barWidth * 2 + gap, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // Etykiety miesięcy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                monthlyData.forEach { data ->
                    Text(
                        data.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(modifier = Modifier.size(12.dp), shape = RoundedCornerShape(2.dp), color = color) {}
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Podsumowanie faktur ───────────────────────────────────────────────────────
@Composable
private fun InvoiceSummaryCard(total: Int, paid: Int) {
    val unpaid = total - paid
    val paidFraction = if (total > 0) paid.toFloat() / total else 0f

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Faktury", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryPill("Łącznie", "$total", MaterialTheme.colorScheme.primary)
                SummaryPill("Opłacone", "$paid", Color(0xFF4CAF50))
                SummaryPill("Nieopłacone", "$unpaid", if (unpaid > 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.outline)
            }
            LinearProgressIndicator(
                progress = { paidFraction },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFFF9800).copy(alpha = 0.3f)
            )
            Text(
                "${(paidFraction * 100).toInt()}% faktur opłaconych",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Kafelki statystyk ─────────────────────────────────────────────────────────
@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: Double, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Text(
                "${String.format("%.2f", value)} PLN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ─── Alert przedawnionych ──────────────────────────────────────────────────────
@Composable
fun OverdueAlertCard(count: Int, amount: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
            Column {
                Text("Przeterminowane faktury!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text("$count faktur na kwotę ${String.format("%.2f", amount)} PLN", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}
