package com.example.e_faktura.ui.invoice.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Invoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

// Stan formularza faktury
data class InvoiceFormState(
    val invoiceNumber: String = "",
    val buyerName: String = "",
    val buyerNip: String = "",
    val netValue: String = "", // String dla pola tekstowego
    val vatRate: String = "23", // %
    val type: String = "SALE", // SALE lub PURCHASE
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceFormState())
    val uiState: StateFlow<InvoiceFormState> = _uiState.asStateFlow()

    fun updateInvoiceNumber(value: String) = _uiState.update { it.copy(invoiceNumber = value) }
    fun updateBuyerName(value: String) = _uiState.update { it.copy(buyerName = value) }
    fun updateBuyerNip(value: String) = _uiState.update { it.copy(buyerNip = value) }

    fun updateNetValue(value: String) = _uiState.update { it.copy(netValue = value) }
    fun updateVatRate(value: String) = _uiState.update { it.copy(vatRate = value) }
    fun updateType(isSale: Boolean) = _uiState.update { it.copy(type = if(isSale) "SALE" else "PURCHASE") }

    fun saveInvoice() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Parsowanie i obliczenia
            val net = state.netValue.toDoubleOrNull() ?: 0.0
            val vatPercent = (state.vatRate.toDoubleOrNull() ?: 23.0) / 100.0

            // Zaokrąglanie do 2 miejsc po przecinku
            val vatVal = (net * vatPercent * 100.0).roundToInt() / 100.0
            val gross = ((net + vatVal) * 100.0).roundToInt() / 100.0

            val newInvoice = Invoice(
                id = UUID.randomUUID().toString(),
                invoiceNumber = state.invoiceNumber,
                buyerName = state.buyerName,
                buyerNip = state.buyerNip,
                netValue = net,
                vatRate = vatPercent,
                vatValue = vatVal,
                grossValue = gross,
                date = state.date,
                type = state.type,
                isPaid = false
            )

            // Teraz to zadziała, bo dodaliśmy metodę do repozytorium w KROKU 2 i 3
            invoiceRepository.insertInvoice(newInvoice)

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun resetState() {
        _uiState.value = InvoiceFormState()
    }
}