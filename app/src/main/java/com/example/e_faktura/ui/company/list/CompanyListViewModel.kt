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
    val companies: List<Company> = emptyList()
)

class CompanyListViewModel(
    companyRepository: CompanyRepository
) : ViewModel() {

    val uiState: StateFlow<CompanyListUiState> = companyRepository.getCompanies()
        .map { CompanyListUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CompanyListUiState()
        )
}