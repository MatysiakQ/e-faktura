package com.example.e_faktura.ui.invoice.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.GusData
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddInvoiceUiState(
    val buyerName: String = "",
    val buyerNip: String = "",
    val buyerAddress: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val amount: String = "",
    val isPaid: Boolean = false,
    val nipToSearch: String = ""
)

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddInvoiceUiState())
    val uiState: StateFlow<AddInvoiceUiState> = _uiState.asStateFlow()

    private val _gusSearchResult = MutableStateFlow<GusData?>(null)
    val gusSearchResult: StateFlow<GusData?> = _gusSearchResult.asStateFlow()

    private val _isLoadingGus = MutableStateFlow(false)
    val isLoadingGus: StateFlow<Boolean> = _isLoadingGus.asStateFlow()

    fun onNipToSearchChange(nip: String) {
        _uiState.update { it.copy(nipToSearch = nip) }
    }

    fun onBuyerNameChange(name: String) {
        _uiState.update { it.copy(buyerName = name) }
    }

    fun onBuyerNipChange(nip: String) {
        _uiState.update { it.copy(buyerNip = nip) }
    }

    fun onBuyerAddressChange(address: String) {
        _uiState.update { it.copy(buyerAddress = address) }
    }

    fun onDateChange(dateMillis: Long) {
        _uiState.update { it.copy(issueDate = dateMillis) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun fetchGusData(nip: String) {
        viewModelScope.launch {
            _isLoadingGus.value = true
            delay(1500) // Simulate network call
            val result = GusData(
                name = "Mock Firma Sp. z o.o.",
                nip = nip,
                address = "ul. Testowa 1, 00-001 Warszawa"
            )
            _gusSearchResult.value = result
            _uiState.update {
                it.copy(
                    buyerName = result.name,
                    buyerNip = result.nip,
                    buyerAddress = result.address
                )
            }
            _isLoadingGus.value = false
        }
    }

    fun addInvoice(onInvoiceAdded: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val invoice = Invoice(
                id = UUID.randomUUID().toString(),
                invoiceNumber = "FV/2024/001", // Placeholder
                buyerName = currentState.buyerName,
                buyerNip = currentState.buyerNip,
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                date = currentState.issueDate,
                isPaid = currentState.isPaid
            )
            invoiceRepository.addInvoice(invoice)
            onInvoiceAdded()
        }
    }
}
