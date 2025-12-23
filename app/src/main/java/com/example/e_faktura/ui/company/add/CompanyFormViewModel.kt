package com.example.e_faktura.ui.company.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.model.Company
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CompanyFormState(
    val id: String? = null,
    val businessName: String = "",
    val nip: String = "",
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "PREDEFINED:Business",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UiEvent {
    object SaveSuccess : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}

@HiltViewModel
class CompanyFormViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyFormState())
    val uiState: StateFlow<CompanyFormState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadCompany(companyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val company = companyRepository.getCompanyById(companyId)
            if (company != null) {
                _uiState.update {
                    it.copy(
                        id = company.id,
                        businessName = company.businessName,
                        nip = company.nip,
                        address = company.address,
                        postalCode = company.postalCode,
                        city = company.city,
                        ownerFullName = company.ownerFullName,
                        bankAccount = company.bankAccount,
                        icon = company.icon,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Nie znaleziono firmy") }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(businessName = name) }
    fun updateNip(nip: String) = _uiState.update { it.copy(nip = nip) }
    fun updateAddress(address: String) = _uiState.update { it.copy(address = address) }
    fun updatePostalCode(code: String) = _uiState.update { it.copy(postalCode = code) }
    fun updateCity(city: String) = _uiState.update { it.copy(city = city) }
    fun updateOwner(owner: String) = _uiState.update { it.copy(ownerFullName = owner) }
    fun updateBankAccount(account: String) = _uiState.update { it.copy(bankAccount = account) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }

    fun searchByNip() {
        val nip = _uiState.value.nip
        if (nip.length != 10) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // GusRepository prawdopodobnie zwraca prosty obiekt z polem 'name' i 'address'
                val result = gusRepository.searchByNip(nip)

                _uiState.update {
                    if (result != null) {
                        it.copy(
                            isLoading = false,
                            businessName = result.name ?: "",
                            address = result.address ?: "",
                            // POPRAWKA: Usunąłem city i postalCode, bo API GUS w Twoim repozytorium ich nie zwraca.
                            // Użytkownik wpisze je ręcznie lub są częścią pola address.
                            city = "",
                            postalCode = ""
                        )
                    } else {
                        it.copy(isLoading = false, error = "Nie znaleziono firmy w GUS dla podanego NIP.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd połączenia z GUS: ${e.message}") }
            }
        }
    }

    fun saveCompany() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.businessName.isBlank() || state.nip.isBlank()) {
                _uiEvent.send(UiEvent.ShowError("Uzupełnij nazwę i NIP"))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            val company = Company(
                id = state.id ?: UUID.randomUUID().toString(),
                businessName = state.businessName,
                nip = state.nip,
                address = state.address,
                postalCode = state.postalCode,
                city = state.city,
                ownerFullName = state.ownerFullName,
                bankAccount = state.bankAccount,
                icon = state.icon
            )

            // Upsert (Insert or Replace)
            companyRepository.insertCompany(company)

            _uiState.update { it.copy(isLoading = false) }
            _uiEvent.send(UiEvent.SaveSuccess)
        }
    }
}