package com.example.e_faktura.ui.invoice.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class InvoiceFilter { ALL, REVENUE, COST, UNPAID, OVERDUE }

data class InvoiceListUiState(
    val allInvoices: List<Invoice> = emptyList(),
    val filteredInvoices: List<Invoice> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: InvoiceFilter = InvoiceFilter.ALL,
    val isLoading: Boolean = true
)

class InvoiceListViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceListUiState(isLoading = true))
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    private fun loadInvoices() {
        viewModelScope.launch {
            invoiceRepository.getInvoices().collect { list ->
                val current = _uiState.value
                _uiState.update {
                    it.copy(
                        allInvoices = list,
                        filteredInvoices = applyFilters(list, current.searchQuery, current.activeFilter),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSearch(query: String) {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredInvoices = applyFilters(s.allInvoices, query, s.activeFilter)
            )
        }
    }

    fun setFilter(filter: InvoiceFilter) {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                activeFilter = filter,
                filteredInvoices = applyFilters(s.allInvoices, s.searchQuery, filter)
            )
        }
    }

    private fun applyFilters(
        invoices: List<Invoice>,
        query: String,
        filter: InvoiceFilter
    ): List<Invoice> {
        val now = System.currentTimeMillis()

        var result = invoices

        // Filtr kategorii
        result = when (filter) {
            InvoiceFilter.ALL     -> result
            InvoiceFilter.REVENUE -> result.filter { it.type == "PRZYCHOD" }
            InvoiceFilter.COST    -> result.filter { it.type == "KOSZT" }
            InvoiceFilter.UNPAID  -> result.filter { it.type == "PRZYCHOD" && !it.isPaid }
            InvoiceFilter.OVERDUE -> result.filter { !it.isPaid && it.dueDate > 0 && it.dueDate < now }
        }

        // Wyszukiwanie tekstowe
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { inv ->
                inv.buyerName.lowercase().contains(q) ||
                inv.buyerNip.contains(q) ||
                inv.invoiceNumber.lowercase().contains(q) ||
                inv.serviceDescription.lowercase().contains(q)
            }
        }

        return result
    }
}
