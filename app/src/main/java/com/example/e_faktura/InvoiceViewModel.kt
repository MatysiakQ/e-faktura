package com.example.e_faktura

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvoiceViewModel : ViewModel() {
    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies = _companies.asStateFlow()

    fun addCompany(company: Company) {
        _companies.value = _companies.value + company
    }

    fun deleteCompany(company: Company) {
        _companies.value = _companies.value - company
    }
}