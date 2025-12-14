package com.example.e_faktura.ui.company.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Data class to hold the UI state for the company list screen.
 */
data class CompanyListUiState(
    val companies: List<Company> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel for the Company List screen.
 */
class CompanyListViewModel(companyRepository: CompanyRepository) : ViewModel() {

    /**
     * The UI state for the CompanyListScreen, exposed as a StateFlow.
     */
    val uiState: StateFlow<CompanyListUiState> = companyRepository.getCompanies()
        .map { companies -> CompanyListUiState(companies = companies) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CompanyListUiState(isLoading = true)
        )
}
