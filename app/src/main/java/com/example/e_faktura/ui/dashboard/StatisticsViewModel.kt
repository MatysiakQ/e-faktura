package com.example.e_faktura.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

data class StatisticsUiState(
    val revenue: Double = 0.0,      // Przychód (Netto sprzedaż)
    val costs: Double = 0.0,        // Koszty (Netto zakup)
    val vatBalance: Double = 0.0,   // Bilans VAT (VAT Sprzedaż - VAT Zakup)
    val pendingAmount: Double = 0.0,// Należności (Brutto nieopłacone)
    val overdueAmount: Double = 0.0,// Przeterminowane (Brutto)
    val overdueCount: Int = 0,
    val currentMonth: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val polishLocale = Locale("pl", "PL")

    val uiState: StateFlow<StatisticsUiState> = invoiceRepository.getInvoices()
        .map { invoices ->
            val now = LocalDate.now()
            val currentMonth = YearMonth.from(now)

            // 1. Filtrowanie po miesiącu
            val monthInvoices = invoices.filter { invoice ->
                val invoiceDate = try {
                    Instant.ofEpochMilli(invoice.date).atZone(ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) { LocalDate.now() }
                YearMonth.from(invoiceDate) == currentMonth
            }

            // 2. Faktury nieopłacone i przeterminowane
            val pendingInvoices = invoices.filter { !it.isPaid }
            val overdueInvoices = pendingInvoices.filter { invoice ->
                val dueDate = try {
                    Instant.ofEpochMilli(invoice.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) { LocalDate.MAX }
                dueDate.isBefore(now)
            }

            // 3. Obliczenia Finansowe (Core logic)
            // Przychód to suma NETTO ze sprzedaży
            val revenue = monthInvoices
                .filter { it.type == "SALE" }
                .sumOf { it.netValue }

            // Koszty to suma NETTO z zakupów
            val costs = monthInvoices
                .filter { it.type == "PURCHASE" }
                .sumOf { it.netValue }

            // VAT Należny (ze sprzedaży)
            val vatSales = monthInvoices
                .filter { it.type == "SALE" }
                .sumOf { it.vatValue }

            // VAT Naliczony (z zakupów)
            val vatPurchases = monthInvoices
                .filter { it.type == "PURCHASE" }
                .sumOf { it.vatValue }

            val vatBalance = vatSales - vatPurchases

            // Nazwa miesiąca
            val monthDisplayName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, polishLocale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(polishLocale) else it.toString() }

            StatisticsUiState(
                revenue = revenue,
                costs = costs,
                vatBalance = vatBalance,
                pendingAmount = pendingInvoices.sumOf { it.grossValue }, // Płatności zawsze w brutto
                overdueAmount = overdueInvoices.sumOf { it.grossValue },
                overdueCount = overdueInvoices.size,
                currentMonth = "$monthDisplayName ${currentMonth.year}",
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState(isLoading = true)
        )
}