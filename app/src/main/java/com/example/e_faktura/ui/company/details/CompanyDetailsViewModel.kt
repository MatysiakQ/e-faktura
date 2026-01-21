package com.example.e_faktura.ui.company.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CompanyDetailsUiState(
    val company: Company? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CompanyDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val companyId: String? = savedStateHandle["companyId"]

    private val _uiState = MutableStateFlow(CompanyDetailsUiState())
    val uiState: StateFlow<CompanyDetailsUiState> = _uiState.asStateFlow()

    init {
        loadCompany()
    }

    private fun loadCompany() {
        if (companyId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Brak ID firmy") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val company = companyRepository.getCompanyById(companyId)
                if (company != null) {
                    _uiState.update { it.copy(company = company, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Nie znaleziono firmy") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd: ${e.message}") }
            }
        }
    }

    // ✅ NOWA FUNKCJA: Logika usuwania wywoływana z UI
    fun deleteCompany(onSuccess: () -> Unit) {
        val company = uiState.value.company ?: return
        viewModelScope.launch {
            try {
                companyRepository.deleteCompany(company)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Nie udało się usunąć: ${e.message}") }
            }
        }
    }
}