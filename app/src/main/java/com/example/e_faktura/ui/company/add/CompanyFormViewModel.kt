package com.example.e_faktura.ui.company.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompanyFormViewModel(
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyUiState())
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Aktualizacja NIP (tylko cyfry, max 10)
    fun updateNip(input: String) {
        val filtered = input.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(nip = filtered, error = null) }
    }

    // Kod pocztowy (XX-XXX)
    fun updatePostalCode(input: String) {
        val digits = input.filter { it.isDigit() }.take(5)
        val formatted = if (digits.length > 2) "${digits.take(2)}-${digits.drop(2)}" else digits
        _uiState.update { it.copy(postalCode = formatted, error = null) }
    }

    // Konto bankowe (spacje co 4 cyfry)
    fun updateBankAccount(input: String) {
        val digits = input.filter { it.isDigit() }.take(26)
        val formatted = StringBuilder()
        for (i in digits.indices) {
            formatted.append(digits[i])
            if (i == 1 || (i > 1 && (i - 1) % 4 == 0 && i < 25)) formatted.append(" ")
        }
        _uiState.update { it.copy(bankAccount = formatted.toString().trim(), error = null) }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name, error = null) }
    fun updateAddress(address: String) = _uiState.update { it.copy(address = address) }
    fun updateCity(city: String) = _uiState.update { it.copy(city = city) }
    fun updateOwner(owner: String) = _uiState.update { it.copy(ownerFullName = owner) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }

    fun loadDataFromNip(nip: String) {
        if (nip.length != 10) {
            _uiState.update { it.copy(error = "NIP musi mieć 10 cyfr") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = gusRepository.searchByNip(nip)
                if (data != null) {
                    _uiState.update { it.copy(
                        name = data.name,
                        address = data.address,
                        city = data.city,
                        postalCode = data.postalCode,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Nie znaleziono firmy w bazie GUS") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd połączenia z bazą GUS") }
            }
        }
    }

    fun saveCompany() {
        val s = uiState.value
        val validationError = when {
            s.nip.length != 10 -> "Podaj 10 cyfr NIP"
            s.name.isBlank() -> "Podaj nazwę firmy"
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val newCompany = Company(
                    id = s.id, nip = s.nip, name = s.name, address = s.address,
                    postalCode = s.postalCode, city = s.city,
                    ownerFullName = s.ownerFullName, bankAccount = s.bankAccount, icon = s.icon
                )
                companyRepository.insertCompany(newCompany)
                _uiEvent.emit(UiEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Błąd zapisu: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}