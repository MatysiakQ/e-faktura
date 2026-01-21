package com.example.e_faktura.ui.company.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CompanyListUiState(
    val companies: List<Company> = emptyList(),
    val isLoading: Boolean = false
)

class CompanyListViewModel(companyRepository: CompanyRepository) : ViewModel() {

    // ZMIANA: getAllCompaniesStream() zamiast getCompanies()
    val uiState: StateFlow<CompanyListUiState> = companyRepository.getAllCompaniesStream()
        .map { companies ->
            CompanyListUiState(companies = companies, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CompanyListUiState(isLoading = true)
        )
}