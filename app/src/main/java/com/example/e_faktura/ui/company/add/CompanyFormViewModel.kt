package com.example.e_faktura.ui.company.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for both Adding and Editing a company.
 * It handles form state and NIP auto-fill logic.
 */
class CompanyFormViewModel(
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val _searchResult = MutableStateFlow<Company?>(null)
    val searchResult: StateFlow<Company?> = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDataFromNip(nip: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResult.value = null // Clear previous results
            try {
                _searchResult.value = gusRepository.searchByNip(nip)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCompany(company: Company) {
        viewModelScope.launch {
            // In a real app, you would check if the company has an ID
            // to decide whether to call addCompany or updateCompany.
            companyRepository.addCompany(company)
        }
    }
}
