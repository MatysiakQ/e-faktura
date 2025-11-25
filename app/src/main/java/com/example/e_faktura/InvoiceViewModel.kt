package com.example.e_faktura

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InvoiceViewModel : ViewModel() {
    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies = _companies.asStateFlow()

    fun addCompany(company: Company) {
        _companies.value = _companies.value + company
    }

    fun deleteCompany(company: Company) {
        _companies.value = _companies.value - company
    }

    fun updateCompanyIcon(company: Company, newIcon: CompanyIcon) {
        _companies.update {
            it.map {
                if (it.nip == company.nip) {
                    it.copy(icon = newIcon)
                } else {
                    it
                }
            }
        }
    }
}