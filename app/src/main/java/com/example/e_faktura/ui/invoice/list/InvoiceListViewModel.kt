package com.example.e_faktura.ui.invoice.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InvoiceListUiState(
    val invoices: List<Invoice> = emptyList(),
    val isLoading: Boolean = false
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
                _uiState.update { it.copy(invoices = list, isLoading = false) }
            }
        }
    }
}