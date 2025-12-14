package com.example.e_faktura.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class StatisticsUiState(
    val revenue: Double = 0.0,
    val costs: Double = 0.0,
    val vatBalance: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val overdueCount: Int = 0,
    val currentMonth: String = "",
    val isLoading: Boolean = false
)

class StatisticsViewModel(
    invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val polishLocale = Locale("pl", "PL")

    val uiState: StateFlow<StatisticsUiState> = invoiceRepository.getInvoices()
        .map { invoices ->
            val now = LocalDate.now()
            val currentMonth = YearMonth.from(now)

            val monthInvoices = invoices.filter { 
                val issueDate = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                YearMonth.from(issueDate) == currentMonth 
            }

            val pendingInvoices = invoices.filter { !it.isPaid }
            val overdueInvoices = pendingInvoices.filter {
                val dueDate = Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                dueDate.isBefore(now)
            }

            val revenue = monthInvoices.filter { it.type == "SALE" }.sumOf { it.amount }
            val costs = monthInvoices.filter { it.type == "COST" }.sumOf { it.amount }
            val vatBalance = (revenue * 0.23) - (costs * 0.23) // Simplified VAT

            val monthDisplayName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, polishLocale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(polishLocale) else it.toString() }

            StatisticsUiState(
                revenue = revenue,
                costs = costs,
                vatBalance = vatBalance,
                pendingAmount = pendingInvoices.sumOf { it.amount },
                overdueAmount = overdueInvoices.sumOf { it.amount },
                overdueCount = overdueInvoices.size,
                currentMonth = "$monthDisplayName ${currentMonth.year}"
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState(isLoading = true)
        )
}
