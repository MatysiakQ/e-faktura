package com.example.e_faktura.ui.invoice.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    // Pobieramy faktury. UI powinno wyświetlać 'grossValue' jako główną kwotę.
    val invoices: StateFlow<List<Invoice>> = invoiceRepository.getInvoices()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Opcjonalnie: funkcja usuwania
    /* fun deleteInvoice(id: String) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(id)
        }
    }
    */
}