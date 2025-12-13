package com.example.e_faktura.ui.company // MOVED: Package is now ui.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CompanyViewModel(companyRepository: CompanyRepository) : ViewModel() {

    val companies: StateFlow<List<Company>> = companyRepository.getCompanies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
