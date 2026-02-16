package com.example.e_faktura.ui.invoice.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.ksef.KsefAuthManager
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Company
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.model.calculateVat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class InvoiceUiState(
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
    val isSaving: Boolean = false,
    val isLoadingGus: Boolean = false,
    val error: String? = null
)

val VAT_RATES = listOf("23", "8", "5", "0", "ZW")
val PAYMENT_METHODS = listOf("PRZELEW", "GOTÓWKA", "KARTA", "KOMPENSATA")

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository,
    private val ksefAuthManager: KsefAuthManager  // ← do pobrania danych sprzedawcy
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState = _uiState.asStateFlow()

    val savedCompanies: StateFlow<List<Company>> = companyRepository.getAllCompaniesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val datePart = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val randomPart = (100..999).random()
        _uiState.update { it.copy(invoiceNumber = "FV/$datePart/$randomPart") }
    }

    fun updateBuyerName(name: String) = _uiState.update { it.copy(buyerName = name, error = null) }
    fun updateBuyerNip(nip: String)   = _uiState.update { it.copy(buyerNip = nip, error = null) }
    fun updateNumber(number: String)  = _uiState.update { it.copy(invoiceNumber = number, error = null) }
    fun updateType(type: String)      = _uiState.update { it.copy(type = type) }
    fun updateServiceDescription(desc: String) = _uiState.update { it.copy(serviceDescription = desc) }
    fun updatePaymentMethod(method: String)    = _uiState.update { it.copy(paymentMethod = method) }

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

    fun selectCompany(company: Company) {
        _uiState.update { it.copy(buyerName = company.displayName, buyerNip = company.nip) }
    }

    fun fetchCompanyFromGus() {
        val nip = _uiState.value.buyerNip
        if (nip.length != 10) {
            _uiState.update { it.copy(error = "NIP musi mieć 10 cyfr") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGus = true, error = null) }
            try {
                val data = gusRepository.searchByNip(nip)
                if (data != null) {
                    _uiState.update { it.copy(buyerName = data.name ?: "", isLoadingGus = false) }
                } else {
                    _uiState.update { it.copy(error = "Nie znaleziono firmy w GUS", isLoadingGus = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Błąd GUS: ${e.message}", isLoadingGus = false) }
            }
        }
    }

    fun saveInvoice(onInvoiceAdded: () -> Unit) {
        val state = _uiState.value

        if (state.invoiceNumber.isBlank()) {
            _uiState.update { it.copy(error = "Podaj numer faktury") }
            return
        }
        val net = state.netAmountInput.toDoubleOrNull()
        if (net == null || net <= 0.0) {
            _uiState.update { it.copy(error = "Podaj prawidłową kwotę netto (większą od 0)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val now = System.currentTimeMillis()
                val twoWeeksMs = 14 * 24 * 60 * 60 * 1000L

                // Pobierz dane sprzedawcy z ustawień KSeF
                val sellerNip  = ksefAuthManager.getNip()
                val sellerName = ksefAuthManager.companyNameFlow.first()

                val invoice = Invoice(
                    id = UUID.randomUUID().toString(),
                    invoiceNumber = state.invoiceNumber,
                    type = state.type,
                    netAmount = net,
                    vatRate = state.vatRate,
                    vatAmount = state.vatAmount,
                    grossAmount = state.grossAmount,
                    amount = state.grossAmount,
                    buyerName = state.buyerName,
                    buyerNip = state.buyerNip,
                    sellerNip = sellerNip,
                    sellerName = sellerName,
                    serviceDescription = state.serviceDescription,
                    paymentMethod = state.paymentMethod,
                    invoiceDate = now,
                    dueDate = now + twoWeeksMs
                )
                invoiceRepository.addInvoice(invoice)
                onInvoiceAdded()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Błąd zapisu: ${e.message}") }
            }
        }
    }
}
