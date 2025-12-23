package com.example.e_faktura.ui.company.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.model.Company
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompanyListUiState(
    val companyList: List<Company> = listOf(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CompanyListViewModel @Inject constructor(
    companyRepository: CompanyRepository
) : ViewModel() {

    val uiState: StateFlow<CompanyListUiState> = companyRepository.getAllCompaniesStream()
        .map { CompanyListUiState(companyList = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CompanyListUiState(isLoading = true)
        )
}
