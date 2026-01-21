package com.example.e_faktura.ui.invoice.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Company
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val invoices: List<Invoice> = emptyList(),
    val companies: List<Company> = emptyList()
)

class InvoiceListViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        invoiceRepository.getInvoices(),
        // ✅ POPRAWKA: getAllCompaniesStream() pasuje do Twojego Repository
        companyRepository.getAllCompaniesStream()
    ) { invoices, companies ->
        HomeUiState(invoices, companies)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}