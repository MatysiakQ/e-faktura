package com.example.e_faktura.ui.invoice.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InvoiceDetailsUiState(
    val invoice: Invoice? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class InvoiceDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceDetailsUiState())
    val uiState: StateFlow<InvoiceDetailsUiState> = _uiState.asStateFlow()

    // Pobieramy ID faktury z argumentów nawigacji
    private val invoiceId: String? = savedStateHandle["invoiceId"]

    init {
        invoiceId?.let { loadInvoice(it) }
    }

    fun loadInvoice(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = invoiceRepository.getInvoiceById(id)
                _uiState.update { it.copy(invoice = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun togglePaidStatus() {
        val currentInvoice = _uiState.value.invoice ?: return
        viewModelScope.launch {
            val updated = currentInvoice.copy(isPaid = !currentInvoice.isPaid)
            invoiceRepository.updateInvoice(updated)
            _uiState.update { it.copy(invoice = updated) }
        }
    }

    fun deleteInvoice(onSuccess: () -> Unit) {
        val currentInvoice = _uiState.value.invoice ?: return
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(currentInvoice)
            onSuccess()
        }
    }
}