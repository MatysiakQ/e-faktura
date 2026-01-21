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
    val isLoading: Boolean = true,
    val error: String? = null
)

class InvoiceDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val invoiceId: String = checkNotNull(savedStateHandle["invoiceId"])
    private val _uiState = MutableStateFlow(InvoiceDetailsUiState())
    val uiState: StateFlow<InvoiceDetailsUiState> = _uiState.asStateFlow()

    init { loadInvoice() }

    private fun loadInvoice() {
        viewModelScope.launch {
            try {
                val invoice = invoiceRepository.getInvoiceById(invoiceId)
                _uiState.update { it.copy(invoice = invoice, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun togglePaidStatus() {
        val currentInvoice = uiState.value.invoice ?: return
        viewModelScope.launch {
            try {
                val updatedInvoice = currentInvoice.copy(isPaid = !currentInvoice.isPaid)
                invoiceRepository.updateInvoice(updatedInvoice)
                _uiState.update { it.copy(invoice = updatedInvoice) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Błąd aktualizacji statusu") }
            }
        }
    }

    fun deleteInvoice(onSuccess: () -> Unit) {
        val invoice = uiState.value.invoice ?: return
        viewModelScope.launch {
            try {
                invoiceRepository.deleteInvoice(invoice)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Błąd usuwania") }
            }
        }
    }
}