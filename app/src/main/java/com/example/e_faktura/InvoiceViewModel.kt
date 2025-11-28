// gotowe
package com.example.e_faktura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: CompanyRepository) : ViewModel() {

    val companies: StateFlow<List<Company>> = repository.getCompanies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCompany(company: Company) {
        viewModelScope.launch {
            repository.addCompany(company)
        }
    }

    fun deleteCompany(company: Company) {
        viewModelScope.launch {
            repository.deleteCompany(company)
        }
    }

    fun updateCompanyIcon(company: Company, newIcon: CompanyIcon) {
        viewModelScope.launch {
            repository.updateCompanyIcon(company, newIcon)
        }
    }
}

class InvoiceViewModelFactory(private val repository: CompanyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}