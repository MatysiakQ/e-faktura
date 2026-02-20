package com.example.e_faktura.ui.invoice.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.model.KsefStatus
import com.example.e_faktura.model.calculateVat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditInvoiceUiState(
    val invoiceNumber: String = "",
    val type: String = "PRZYCHOD",
    val buyerName: String = "",
    val buyerNip: String = "",
    val netAmountInput: String = "",
    val vatRate: String = "23",
    val vatAmount: Double = 0.0,
    val grossAmount: Double = 0.0,
    val serviceDescription: String = "",
    val paymentMethod: String = "PRZELEW",
    // BUG #9 FIX: pola dat edytowalne
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val notFound: Boolean = false,
    val error: String? = null
)

sealed class EditInvoiceEvent {
    object SaveSuccess : EditInvoiceEvent()
    data class ShowError(val message: String) : EditInvoiceEvent()
}

class EditInvoiceViewModel(
    savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val invoiceId: String? = savedStateHandle["invoiceId"]
    private var originalInvoice: Invoice? = null

    private val _uiState = MutableStateFlow(EditInvoiceUiState())
    val uiState: StateFlow<EditInvoiceUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditInvoiceEvent>()
    val events = _events.asSharedFlow()

    init { loadInvoice() }

    private fun loadInvoice() {
        if (invoiceId == null) {
            _uiState.update { it.copy(isLoading = false, notFound = true) }
            return
        }
        viewModelScope.launch {
            try {
                val invoice = invoiceRepository.getInvoiceById(invoiceId)
                if (invoice != null) {
                    originalInvoice = invoice
                    val net = if (invoice.netAmount > 0) invoice.netAmount else invoice.amount
                    _uiState.update {
                        it.copy(
                            invoiceNumber = invoice.invoiceNumber,
                            type = invoice.type,
                            buyerName = invoice.buyerName,
                            buyerNip = invoice.buyerNip,
                            netAmountInput = String.format("%.2f", net).replace(",", "."),
                            vatRate = invoice.vatRate,
                            vatAmount = invoice.vatAmount,
                            grossAmount = if (invoice.grossAmount > 0) invoice.grossAmount else invoice.amount,
                            serviceDescription = invoice.serviceDescription,
                            paymentMethod = invoice.paymentMethod,
                            invoiceDate = if (invoice.invoiceDate > 0) invoice.invoiceDate else System.currentTimeMillis(),
                            dueDate = if (invoice.dueDate > 0) invoice.dueDate else System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd: ${e.message}") }
            }
        }
    }

    fun updateBuyerName(name: String) = _uiState.update { it.copy(buyerName = name, error = null) }
    fun updateBuyerNip(nip: String)   = _uiState.update { it.copy(buyerNip = nip, error = null) }
    fun updateNumber(n: String)       = _uiState.update { it.copy(invoiceNumber = n, error = null) }
    fun updateType(t: String)         = _uiState.update { it.copy(type = t) }
    fun updateServiceDescription(d: String) = _uiState.update { it.copy(serviceDescription = d) }
    fun updatePaymentMethod(m: String) = _uiState.update { it.copy(paymentMethod = m) }
    // BUG #9 FIX: metody dat
    fun updateInvoiceDate(epochMs: Long) = _uiState.update { it.copy(invoiceDate = epochMs) }
    fun updateDueDate(epochMs: Long)     = _uiState.update { it.copy(dueDate = epochMs) }

    fun updateNetAmount(input: String) {
        val net = input.toDoubleOrNull() ?: 0.0
        val vat = calculateVat(net, _uiState.value.vatRate)
        _uiState.update { it.copy(netAmountInput = input, vatAmount = vat, grossAmount = net + vat) }
    }

    fun updateVatRate(rate: String) {
        val net = _uiState.value.netAmountInput.toDoubleOrNull() ?: 0.0
        val vat = calculateVat(net, rate)
        _uiState.update { it.copy(vatRate = rate, vatAmount = vat, grossAmount = net + vat) }
    }

    fun saveChanges() {
        val s = _uiState.value
        if (s.invoiceNumber.isBlank()) {
            _uiState.update { it.copy(error = "Podaj numer faktury") }
            return
        }
        val net = s.netAmountInput.toDoubleOrNull()
        if (net == null || net <= 0.0) {
            _uiState.update { it.copy(error = "Podaj prawidłową kwotę netto") }
            return
        }
        val original = originalInvoice ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updated = original.copy(
                    invoiceNumber = s.invoiceNumber,
                    type = s.type,
                    buyerName = s.buyerName,
                    buyerNip = s.buyerNip,
                    netAmount = net,
                    vatRate = s.vatRate,
                    vatAmount = s.vatAmount,
                    grossAmount = s.grossAmount,
                    amount = s.grossAmount,
                    serviceDescription = s.serviceDescription,
                    paymentMethod = s.paymentMethod,
                    invoiceDate = s.invoiceDate,
                    dueDate = s.dueDate,
                    // Reset KSeF jeśli edytujemy odrzuconą/lokalną — zaakceptowanej nie ruszamy
                    ksefStatus = if (original.ksefStatus == KsefStatus.ACCEPTED.name)
                        KsefStatus.ACCEPTED.name else KsefStatus.LOCAL.name,
                    ksefReferenceNumber = if (original.ksefStatus == KsefStatus.ACCEPTED.name)
                        original.ksefReferenceNumber else ""
                )
                invoiceRepository.updateInvoice(updated)
                _events.emit(EditInvoiceEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(EditInvoiceEvent.ShowError("Błąd zapisu: ${e.message}"))
            }
        }
    }
}
