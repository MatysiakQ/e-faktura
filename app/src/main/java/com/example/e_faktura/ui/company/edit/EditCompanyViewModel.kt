package com.example.e_faktura.ui.company.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditCompanyUiState(
    val id: String = "",
    val nip: String = "",
    val name: String = "",
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "VECTOR:Business",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val notFound: Boolean = false
)

sealed class EditCompanyEvent {
    object SaveSuccess : EditCompanyEvent()
    data class ShowError(val message: String) : EditCompanyEvent()
}

class EditCompanyViewModel(
    savedStateHandle: SavedStateHandle,
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val companyId: String? = savedStateHandle["companyId"]

    private val _uiState = MutableStateFlow(EditCompanyUiState())
    val uiState: StateFlow<EditCompanyUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditCompanyEvent>()
    val events = _events.asSharedFlow()

    init {
        loadCompany()
    }

    private fun loadCompany() {
        if (companyId == null) {
            _uiState.update { it.copy(isLoading = false, notFound = true) }
            return
        }
        viewModelScope.launch {
            try {
                val company = companyRepository.getCompanyById(companyId)
                if (company != null) {
                    _uiState.update {
                        it.copy(
                            id = company.id,
                            nip = company.nip,
                            name = company.name ?: "",
                            address = company.address,
                            postalCode = company.postalCode,
                            city = company.city,
                            ownerFullName = company.ownerFullName ?: "",
                            bankAccount = company.bankAccount,
                            icon = company.icon.ifBlank { "VECTOR:Business" },
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

    fun updateNip(input: String) {
        val filtered = input.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(nip = filtered, error = null) }
    }

    fun updatePostalCode(input: String) {
        val digits = input.filter { it.isDigit() }.take(5)
        val formatted = if (digits.length > 2) "${digits.take(2)}-${digits.drop(2)}" else digits
        _uiState.update { it.copy(postalCode = formatted) }
    }

    fun updateBankAccount(input: String) {
        val digits = input.filter { it.isDigit() }.take(26)
        val formatted = StringBuilder()
        for (i in digits.indices) {
            formatted.append(digits[i])
            if (i == 1 || (i > 1 && (i - 1) % 4 == 0 && i < 25)) formatted.append(" ")
        }
        _uiState.update { it.copy(bankAccount = formatted.toString().trim()) }
    }

    fun updateName(name: String)    = _uiState.update { it.copy(name = name, error = null) }
    fun updateAddress(a: String)    = _uiState.update { it.copy(address = a) }
    fun updateCity(c: String)       = _uiState.update { it.copy(city = c) }
    fun updateOwner(o: String)      = _uiState.update { it.copy(ownerFullName = o) }
    fun updateIcon(icon: String)    = _uiState.update { it.copy(icon = icon) }

    fun fetchFromGus() {
        val nip = _uiState.value.nip
        if (nip.length != 10) {
            _uiState.update { it.copy(error = "NIP musi mieć 10 cyfr") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val data = gusRepository.searchByNip(nip)
                if (data != null) {
                    _uiState.update {
                        it.copy(
                            name = if (data.name.isNullOrBlank()) it.name else data.name,
                            address = if (data.address.isNullOrBlank()) it.address else data.address,
                            city = if (data.city.isNullOrBlank()) it.city else data.city,
                            postalCode = if (data.postalCode.isNullOrBlank()) it.postalCode else data.postalCode,
                            bankAccount = if (data.bankAccount.isNullOrBlank()) it.bankAccount else data.bankAccount,
                            isSaving = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Nie znaleziono w bazie MF") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Błąd GUS: ${e.message}") }
            }
        }
    }

    fun saveChanges() {
        val s = _uiState.value
        if (s.nip.length != 10) {
            _uiState.update { it.copy(error = "NIP musi mieć 10 cyfr") }
            return
        }
        if (s.name.isBlank()) {
            _uiState.update { it.copy(error = "Podaj nazwę firmy") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updated = Company(
                    id = s.id,
                    nip = s.nip,
                    name = s.name,
                    address = s.address,
                    postalCode = s.postalCode,
                    city = s.city,
                    ownerFullName = s.ownerFullName,
                    bankAccount = s.bankAccount,
                    icon = s.icon
                )
                companyRepository.updateCompany(updated)
                _events.emit(EditCompanyEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(EditCompanyEvent.ShowError("Błąd zapisu: ${e.message}"))
            }
        }
    }
}
