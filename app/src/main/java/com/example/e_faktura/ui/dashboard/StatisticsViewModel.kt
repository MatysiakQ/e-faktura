package com.example.e_faktura.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class MonthlyData(
    val label: String,       // np. "Sty", "Lut"
    val revenue: Double,
    val costs: Double
)

data class StatisticsUiState(
    val revenue: Double = 0.0,
    val costs: Double = 0.0,
    val profit: Double = 0.0,
    val vatOwed: Double = 0.0,
    val vatPaid: Double = 0.0,
    val vatBalance: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val overdueCount: Int = 0,
    val overdueAmount: Double = 0.0,
    val currentMonth: String = "",
    val monthlyData: List<MonthlyData> = emptyList(),
    val totalInvoices: Int = 0,
    val paidInvoices: Int = 0,
    val isLoading: Boolean = true
)

class StatisticsViewModel(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init { loadStatistics() }

    private fun Invoice.getAmount(): Double = if (grossAmount > 0) grossAmount else amount
    private fun Invoice.getNet(): Double = if (netAmount > 0) netAmount else amount
    private fun Invoice.getVat(): Double = vatAmount

    private fun loadStatistics() {
        viewModelScope.launch {
            invoiceRepository.getInvoices().collect { invoices ->
                val now = LocalDate.now()
                val zoneId = ZoneId.systemDefault()
                val monthName = now.month.getDisplayName(TextStyle.FULL, Locale("pl"))

                // ── Bieżący miesiąc ──────────────────────────────────────────
                val rev = invoices.filter { it.type == "PRZYCHOD" && it.isPaid }.sumOf { it.getAmount() }
                val cst = invoices.filter { it.type == "KOSZT" }.sumOf { it.getAmount() }

                // VAT: od przychodów opłaconych minus VAT od kosztów
                val vatOwed = invoices.filter { it.type == "PRZYCHOD" && it.isPaid }.sumOf { it.getVat() }
                val vatPaid = invoices.filter { it.type == "KOSZT" }.sumOf { it.getVat() }
                val vatBalance = vatOwed - vatPaid

                val pending = invoices.filter { it.type == "PRZYCHOD" && !it.isPaid }.sumOf { it.getAmount() }

                val overdue = invoices.filter { inv ->
                    if (inv.isPaid) return@filter false
                    if (inv.dueDate <= 0) return@filter false
                    val due = Instant.ofEpochMilli(inv.dueDate).atZone(zoneId).toLocalDate()
                    due.isBefore(now)
                }

                // ── Dane miesięczne (ostatnie 6 miesięcy) ────────────────────
                val monthlyData = buildMonthlyData(invoices, now, zoneId)

                _uiState.update {
                    it.copy(
                        revenue = rev,
                        costs = cst,
                        profit = rev - cst,
                        vatOwed = vatOwed,
                        vatPaid = vatPaid,
                        vatBalance = vatBalance,
                        pendingAmount = pending,
                        overdueCount = overdue.size,
                        overdueAmount = overdue.sumOf { it.getAmount() },
                        currentMonth = "$monthName ${now.year}",
                        monthlyData = monthlyData,
                        totalInvoices = invoices.size,
                        paidInvoices = invoices.count { it.isPaid },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun buildMonthlyData(
        invoices: List<Invoice>,
        now: LocalDate,
        zoneId: ZoneId
    ): List<MonthlyData> {
        return (5 downTo 0).map { monthsBack ->
            val yearMonth = YearMonth.from(now).minusMonths(monthsBack.toLong())
            val label = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale("pl"))
                .replaceFirstChar { it.uppercase() }

            val monthInvoices = invoices.filter { inv ->
                if (inv.invoiceDate <= 0) return@filter false
                val invoiceMonth = Instant.ofEpochMilli(inv.invoiceDate)
                    .atZone(zoneId).toLocalDate()
                YearMonth.from(invoiceMonth) == yearMonth
            }

            MonthlyData(
                label = label,
                revenue = monthInvoices.filter { it.type == "PRZYCHOD" && it.isPaid }
                    .sumOf { if (it.grossAmount > 0) it.grossAmount else it.amount },
                costs = monthInvoices.filter { it.type == "KOSZT" }
                    .sumOf { if (it.grossAmount > 0) it.grossAmount else it.amount }
            )
        }
    }
}
