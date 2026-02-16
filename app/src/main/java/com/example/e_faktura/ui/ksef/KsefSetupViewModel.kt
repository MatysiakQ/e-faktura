package com.example.e_faktura.ui.ksef

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_faktura.data.ksef.KsefRepository
import com.example.e_faktura.data.ksef.KsefResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class KsefSetupUiState(
    val nip: String = "",
    val ksefToken: String = "",
    val companyName: String = "",
    val isConnected: Boolean = false,
    val isProduction: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class KsefSetupViewModel(
    private val ksefRepository: KsefRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KsefSetupUiState())
    val uiState: StateFlow<KsefSetupUiState> = _uiState.asStateFlow()

    init {
        // Obserwuj dane z DataStore i aktualizuj stan UI
        viewModelScope.launch {
            combine(
                ksefRepository.isConnected,
                ksefRepository.nipFlow,
                ksefRepository.companyNameFlow,
                ksefRepository.isProductionFlow
            ) { isConnected, nip, companyName, isProd ->
                _uiState.update {
                    it.copy(
                        isConnected = isConnected,
                        nip = nip,
                        companyName = companyName,
                        isProduction = isProd
                    )
                }
            }.collect()
        }
    }

    fun updateNip(nip: String) {
        _uiState.update { it.copy(nip = nip, error = null) }
    }

    fun updateKsefToken(token: String) {
        _uiState.update { it.copy(ksefToken = token, error = null) }
    }

    fun updateCompanyName(name: String) {
        _uiState.update { it.copy(companyName = name) }
    }

    fun setEnvironment(isProduction: Boolean) {
        viewModelScope.launch {
            ksefRepository.setEnvironment(isProduction)
            _uiState.update { it.copy(isProduction = isProduction) }
        }
    }

    /** Weryfikuje połączenie z KSeF API (bez autoryzacji) */
    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = ksefRepository.checkApiHealth()
            _uiState.update {
                when (result) {
                    is KsefResult.Success -> it.copy(
                        isLoading = false,
                        successMessage = "✓ API KSeF jest dostępne"
                    )
                    is KsefResult.Error -> it.copy(
                        isLoading = false,
                        error = result.message
                    )
                    else -> it.copy(isLoading = false)
                }
            }
        }
    }

    /** Pełna autoryzacja — wymaga NIP + token KSeF */
    fun authorize() {
        val state = _uiState.value

        // Walidacja NIP
        val cleanNip = state.nip.filter { it.isDigit() }
        if (cleanNip.length != 10) {
            _uiState.update { it.copy(error = "NIP musi mieć dokładnie 10 cyfr") }
            return
        }

        // Walidacja tokenu KSeF
        if (state.ksefToken.isBlank()) {
            _uiState.update { it.copy(error = "Wpisz token KSeF wygenerowany w portalu podatki.gov.pl") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = ksefRepository.authorize(cleanNip, state.ksefToken, state.companyName)
            _uiState.update {
                when (result) {
                    is KsefResult.Success -> it.copy(
                        isLoading = false,
                        isConnected = true,
                        successMessage = "✓ Połączono z KSeF pomyślnie!",
                        ksefToken = "" // Wyczyść pole tokenu po zapisaniu
                    )
                    is KsefResult.Error -> it.copy(
                        isLoading = false,
                        error = result.message
                    )
                    else -> it.copy(isLoading = false)
                }
            }
        }
    }

    /** Rozłącz sesję KSeF */
    fun disconnect() {
        viewModelScope.launch {
            ksefRepository.disconnect()
            _uiState.update { it.copy(successMessage = "Rozłączono z KSeF") }
        }
    }

    /** Usuń wszystkie dane KSeF */
    fun clearAllData() {
        viewModelScope.launch {
            ksefRepository.clearAllData()
            _uiState.update {
                KsefSetupUiState(successMessage = "Dane KSeF zostały usunięte")
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
