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
import java.time.LocalDate
import java.util.UUID

data class AddInvoiceUiState(
    val buyerName: String = "",
    val buyerNip: String = "",
    val buyerAddress: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val paymentDueDate: Long = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000, // Default to 14 days later
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

    private val _nextInvoiceNumber = MutableStateFlow("")
    val nextInvoiceNumber: StateFlow<String> = _nextInvoiceNumber.asStateFlow()

    init {
        generateNextInvoiceNumber()
    }

    private fun generateNextInvoiceNumber() {
        // In a real app, you'd fetch the last invoice number from the repository
        val year = LocalDate.now().year
        val month = LocalDate.now().monthValue
        // This is a placeholder. A real implementation needs a proper sequence.
        val nextId = (Math.random() * 100).toInt() + 1 
        _nextInvoiceNumber.value = "FV/$year/$month/${String.format("%02d", nextId)}"
    }

    fun onNipToSearchChange(nip: String) {
        _uiState.update { it.copy(nipToSearch = nip) }
    }

    fun onBuyerNameChange(name: String) {
        _uiState.update { it.copy(buyerName = name) }
    }

    fun onIssueDateChange(dateMillis: Long) {
        _uiState.update { it.copy(issueDate = dateMillis) }
    }

    fun onPaymentDueDateChange(dateMillis: Long) {
        _uiState.update { it.copy(paymentDueDate = dateMillis) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun fetchGusData(nip: String) {
        viewModelScope.launch {
            _isLoadingGus.value = true
            delay(1500) // Simulate network call
            val result = GusData(
                name = "Mock Klient Sp. z o.o.",
                nip = nip,
                address = "ul. Klientów 123, 00-002 Warszawa"
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

    fun issueInvoice(onInvoiceIssued: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val invoice = Invoice(
                id = UUID.randomUUID().toString(),
                invoiceNumber = _nextInvoiceNumber.value, 
                buyerName = currentState.buyerName,
                buyerNip = currentState.buyerNip,
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                date = currentState.issueDate,
                // In a real app, you might want to store the due date as well
                isPaid = false 
            )
            invoiceRepository.addInvoice(invoice)
            // Regenerate number for the next invoice
            generateNextInvoiceNumber()
            onInvoiceIssued()
        }
    }
}
