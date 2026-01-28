package com.example.e_faktura.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // ✅ Zmieniono z hilt
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider // ✅ Dodano naszą fabrykę

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onOverdueClick: () -> Unit
) {
    val uiState by statisticsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statystyki") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Wyniki za ${uiState.currentMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(modifier = Modifier.weight(1f), data = StatCardInfo("Przychody", uiState.revenue, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF4CAF50)))
                        StatCard(modifier = Modifier.weight(1f), data = StatCardInfo("Koszty", uiState.costs, Icons.AutoMirrored.Filled.TrendingDown, Color(0xFFF44336)))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(modifier = Modifier.weight(1f), data = StatCardInfo("Bilans VAT", uiState.vatBalance, Icons.Filled.AccountBalance, Color(0xFF2196F3)))
                        StatCard(modifier = Modifier.weight(1f), data = StatCardInfo("Oczekujące", uiState.pendingAmount, Icons.Filled.HourglassEmpty, Color(0xFFFF9800)))
                    }
                }

                if (uiState.overdueCount > 0) {
                    item {
                        OverdueAlertCard(
                            count = uiState.overdueCount,
                            amount = uiState.overdueAmount,
                            onClick = onOverdueClick
                        )
                    }
                }
            }
        }
    }
}

data class StatCardInfo(
    val label: String,
    val value: Double,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun StatCard(modifier: Modifier = Modifier, data: StatCardInfo) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = data.color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = data.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Icon(imageVector = data.icon, contentDescription = data.label, tint = data.color, modifier = Modifier.size(28.dp))
            }
            Text(
                text = "${String.format("%.2f", data.value)} PLN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = data.color
            )
        }
    }
}

@Composable
fun OverdueAlertCard(count: Int, amount: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = Icons.Filled.Warning, contentDescription = "Ostrzeżenie", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
            Column {
                Text(text = "Przeterminowane faktury!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text(text = "$count faktur na kwotę ${String.format("%.2f", amount)} PLN", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}