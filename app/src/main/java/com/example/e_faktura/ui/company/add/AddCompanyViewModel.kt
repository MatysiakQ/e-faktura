package com.example.e_faktura.ui.company.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// CORRECTED: Imports from the standardized data.repository package
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CORRECTED ViewModel for adding a company.
 * This now uses the standardized repositories and models.
 */
class AddCompanyViewModel(
    private val companyRepository: CompanyRepository,
    private val gusRepository: GusRepository
) : ViewModel() {

    private val _searchResult = MutableStateFlow<Company?>(null)
    val searchResult: StateFlow<Company?> = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Searches GUS using the standardized repository.
     * The result is a nullable Company object, which is posted to the UI.
     */
    fun searchGus(nip: String) {
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

    /**
     * CORRECTED: This function now accepts the one, true Company model.
     */
    fun addCompany(company: Company) {
        viewModelScope.launch {
            companyRepository.addCompany(company)
        }
    }
}
