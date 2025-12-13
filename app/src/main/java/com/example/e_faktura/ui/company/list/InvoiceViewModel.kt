package com.example.e_faktura.ui.company.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// CORRECTED: Imports now point to the correct data.repository package
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

class InvoiceViewModel(
    invoiceRepository: InvoiceRepository,
    companyRepository: CompanyRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        // CORRECTED: Call the getInvoices() function instead of the property
        invoiceRepository.getInvoices(),
        // CORRECTED: Call the getCompanies() function instead of the property
        companyRepository.getCompanies()
    ) { invoices, companies ->
        HomeUiState(invoices, companies)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
