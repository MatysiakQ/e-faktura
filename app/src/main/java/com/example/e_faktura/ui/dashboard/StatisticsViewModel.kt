package com.example.e_faktura.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class StatisticsUiState(
    val revenue: Double = 0.0,
    val costs: Double = 0.0,
    val vatBalance: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val overdueCount: Int = 0,
    val overdueAmount: Double = 0.0,
    val currentMonth: String = "",
    val isLoading: Boolean = false
)

class StatisticsViewModel(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            invoiceRepository.getInvoices().collect { invoices ->
                val now = LocalDate.now()
                val monthName = now.month.getDisplayName(TextStyle.FULL, Locale("pl"))

                val rev = invoices.filter { it.type == "PRZYCHOD" }.sumOf { it.amount }
                val cst = invoices.filter { it.type == "KOSZT" }.sumOf { it.amount }

                // ✅ NAPRAWIONO: Konwersja Long (timestamp) na LocalDate
                val overdue = invoices.filter { invoice ->
                    if (invoice.isPaid) return@filter false

                    val dueLocalDate = Instant.ofEpochMilli(invoice.dueDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    dueLocalDate.isBefore(now)
                }

                _uiState.update {
                    it.copy(
                        revenue = rev,
                        costs = cst,
                        vatBalance = (rev - cst) * 0.23,
                        pendingAmount = invoices.filter { !it.isPaid }.sumOf { it.amount },
                        overdueCount = overdue.size,
                        overdueAmount = overdue.sumOf { it.amount },
                        currentMonth = "$monthName ${now.year}",
                        isLoading = false
                    )
                }
            }
        }
    }
}