package com.example.e_faktura.ui.invoice.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.GusData
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _gusSearchResult = MutableStateFlow<GusData?>(null)
    val gusSearchResult: StateFlow<GusData?> = _gusSearchResult

    private val _isLoadingGus = MutableStateFlow(false)
    val isLoadingGus: StateFlow<Boolean> = _isLoadingGus

    fun fetchGusData(nip: String) {
        viewModelScope.launch {
            _isLoadingGus.value = true
            // Simulate network call
            kotlinx.coroutines.delay(1000)
            _gusSearchResult.value = GusData(businessName = "Fake Company from GUS")
            _isLoadingGus.value = false
        }
    }

    fun addInvoice(invoice: Invoice) {
        viewModelScope.launch {
            invoiceRepository.addInvoice(invoice)
        }
    }
}