package com.example.e_faktura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InvoiceViewModel : ViewModel() {

    private val repository = CompanyRepository()

    val companies: StateFlow<List<Company>> = repository.getCompanies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCompany(company: Company) {
        repository.addCompany(company)
    }

    fun deleteCompany(company: Company) {
        repository.deleteCompany(company)
    }

    fun updateCompanyIcon(company: Company, newIcon: CompanyIcon) {
        repository.updateCompanyIcon(company, newIcon)
    }
}