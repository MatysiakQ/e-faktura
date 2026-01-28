package com.example.e_faktura.ui.company.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class CompanyUiState(
    val id: String = UUID.randomUUID().toString(),
    val nip: String = "",
    val name: String = "",
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "VECTOR:Business",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UiEvent {
    object SaveSuccess : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}

class CompanyFormViewModel(
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyUiState())
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun updateNip(input: String) {
        val filtered = input.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(nip = filtered, error = null) }
    }

    // ✅ NAPRAWIONO: Dodano brakującą metodę dla AddCompanyScreen
    fun updatePostalCode(input: String) {
        val digits = input.filter { it.isDigit() }.take(5)
        val formatted = if (digits.length > 2) "${digits.take(2)}-${digits.drop(2)}" else digits
        _uiState.update { it.copy(postalCode = formatted) }
    }

    // ✅ NAPRAWIONO: Dodano brakującą metodę dla AddCompanyScreen
    fun updateBankAccount(input: String) {
        val digits = input.filter { it.isDigit() }.take(26)
        val formatted = StringBuilder()
        for (i in digits.indices) {
            formatted.append(digits[i])
            if (i == 1 || (i > 1 && (i - 1) % 4 == 0 && i < 25)) formatted.append(" ")
        }
        _uiState.update { it.copy(bankAccount = formatted.toString().trim()) }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateAddress(address: String) = _uiState.update { it.copy(address = address) }
    fun updateCity(city: String) = _uiState.update { it.copy(city = city) }
    fun updateOwner(owner: String) = _uiState.update { it.copy(ownerFullName = owner) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }

    fun loadDataFromNip(nip: String) {
        if (nip.length != 10) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = gusRepository.searchByNip(nip)
                if (data != null) {
                    _uiState.update { it.copy(
                        // ✅ NAPRAWIONO: Operator ?: "" eliminuje błąd Argument type mismatch
                        name = data.name ?: "",
                        address = data.address ?: "",
                        city = data.city ?: "",
                        postalCode = data.postalCode ?: "",
                        bankAccount = data.bankAccount ?: it.bankAccount,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Brak podmiotu w bazie MF") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd połączenia") }
            }
        }
    }

    fun saveCompany() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val s = uiState.value
                val company = Company(
                    id = s.id, nip = s.nip, name = s.name, address = s.address,
                    postalCode = s.postalCode, city = s.city, ownerFullName = s.ownerFullName,
                    bankAccount = s.bankAccount, icon = s.icon, userId = ""
                )
                companyRepository.insertCompany(company)
                _uiEvent.emit(UiEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Błąd zapisu"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}