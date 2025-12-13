package com.example.e_faktura.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Company
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// CORRECTED: The constructor now accepts all three required repositories
class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val gusRepository: GusRepository,
    companyRepository: CompanyRepository // This was the missing argument
) : ViewModel() {

    // --- For the Dashboard Screen ---
    val invoices: StateFlow<List<Invoice>> = invoiceRepository.getInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val realRevenueLast30Days: StateFlow<Double> = invoices.map { invoiceList ->
        val thirtyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.timeInMillis
        invoiceList.filter { it.isPaid && it.date >= thirtyDaysAgo }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    // --- For the Add Invoice Screen ---
    private val _gusSearchResult = MutableStateFlow<Company?>(null)
    val gusSearchResult: StateFlow<Company?> = _gusSearchResult.asStateFlow()

    private val _isLoadingGus = MutableStateFlow(false)
    val isLoadingGus: StateFlow<Boolean> = _isLoadingGus.asStateFlow()

    // Exposes the user's companies for the "Seller" dropdown
    val myCompanies: StateFlow<List<Company>> = companyRepository.getCompanies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSeller = MutableStateFlow<Company?>(null)
    val selectedSeller: StateFlow<Company?> = _selectedSeller.asStateFlow()

    init {
        // Auto-select the seller if there's only one company available
        viewModelScope.launch {
            myCompanies.collect { companies ->
                if (companies.size == 1 && _selectedSeller.value == null) {
                    _selectedSeller.value = companies.first()
                }
            }
        }
    }

    fun onSellerSelected(company: Company) {
        _selectedSeller.value = company
    }

    fun fetchGusData(nip: String) {
        viewModelScope.launch {
            _isLoadingGus.value = true
            _gusSearchResult.value = null
            try {
                _gusSearchResult.value = gusRepository.searchByNip(nip)
            } finally {
                _isLoadingGus.value = false
            }
        }
    }

    fun addInvoice(invoice: Invoice) {
        viewModelScope.launch {
            invoiceRepository.addInvoice(invoice)
        }
    }
}
