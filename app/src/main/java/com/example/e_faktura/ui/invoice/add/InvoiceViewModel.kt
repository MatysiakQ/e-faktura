package com.example.e_faktura.ui.invoice.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.model.Company
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // ✅ DODANO
import java.util.* // ✅ DODANO

data class InvoiceUiState(
    val buyerName: String = "",
    val buyerNip: String = "",
    val amount: String = "",
    val invoiceNumber: String = "",
    val isSaving: Boolean = false,
    val isLoadingGus: Boolean = false,
    val error: String? = null
)

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState = _uiState.asStateFlow()

    val savedCompanies: StateFlow<List<Company>> = companyRepository.getAllCompaniesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ DODANO: Automatyczne numerowanie przy otwarciu formularza
    init {
        val datePart = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val randomPart = (10..99).random()
        _uiState.update { it.copy(invoiceNumber = "FV/$datePart/$randomPart") }
    }

    fun updateBuyerName(name: String) { _uiState.update { it.copy(buyerName = name, error = null) } }
    fun updateBuyerNip(nip: String) { _uiState.update { it.copy(buyerNip = nip, error = null) } }
    fun updateAmount(amount: String) { _uiState.update { it.copy(amount = amount) } }
    fun updateNumber(number: String) { _uiState.update { it.copy(invoiceNumber = number, error = null) } }

    fun selectCompany(company: Company) {
        _uiState.update { it.copy(buyerName = company.name, buyerNip = company.nip) }
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
                    _uiState.update { it.copy(buyerName = data.name, isLoadingGus = false) }
                } else {
                    _uiState.update { it.copy(error = "Nie znaleziono firmy w GUS", isLoadingGus = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Błąd: ${e.message}", isLoadingGus = false) }
            }
        }
    }

    fun saveInvoice(onInvoiceAdded: () -> Unit) {
        // ✅ DODANO: Walidacja numeru faktury
        if (_uiState.value.invoiceNumber.isBlank()) {
            _uiState.update { it.copy(error = "Podaj numer faktury") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val invoice = Invoice(
                    id = UUID.randomUUID().toString(),
                    invoiceNumber = _uiState.value.invoiceNumber, // Pobieramy ze stanu
                    buyerName = _uiState.value.buyerName,
                    buyerNip = _uiState.value.buyerNip,
                    amount = _uiState.value.amount.toDoubleOrNull() ?: 0.0,
                    dueDate = System.currentTimeMillis()
                )
                invoiceRepository.addInvoice(invoice)
                onInvoiceAdded()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Błąd zapisu") }
            }
        }
    }
}