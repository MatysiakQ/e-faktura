package com.example.e_faktura.ui.company.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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
            _uiState.value = CompanyDetailsUiState(isLoading = false, error = "Nie znaleziono firmy")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val companyFlow = companyRepository.getCompanies().map {
                    it.firstOrNull { company -> company.id == companyId }
                }
                val company = companyFlow.firstOrNull()
                if (company != null) {
                    _uiState.value = CompanyDetailsUiState(company = company, isLoading = false)
                } else {
                    _uiState.value = CompanyDetailsUiState(isLoading = false, error = "Nie znaleziono firmy o podanym ID")
                }
            } catch (e: Exception) {
                _uiState.value = CompanyDetailsUiState(isLoading = false, error = "Błąd wczytywania danych: ${e.message}")
            }
        }
    }
}
