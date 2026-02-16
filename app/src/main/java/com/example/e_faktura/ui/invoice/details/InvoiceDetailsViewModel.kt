package com.example.e_faktura.ui.invoice.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.e_faktura.data.ksef.KsefRepository
import com.example.e_faktura.data.ksef.KsefResult
import com.example.e_faktura.data.repository.InvoiceRepository
import com.example.e_faktura.data.worker.KsefStatusWorker
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.model.KsefStatus
import com.example.e_faktura.utils.KsefXmlGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class InvoiceDetailsUiState(
    val invoice: Invoice? = null,
    val isLoading: Boolean = false,
    val isSendingToKsef: Boolean = false,
    val ksefMessage: String? = null,
    val error: String? = null
)

class InvoiceDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository,
    private val ksefRepository: KsefRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceDetailsUiState())
    val uiState: StateFlow<InvoiceDetailsUiState> = _uiState.asStateFlow()

    private val invoiceId: String? = savedStateHandle["invoiceId"]

    init {
        invoiceId?.let { loadInvoice(it) }
    }

    fun loadInvoice(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = invoiceRepository.getInvoiceById(id)
                _uiState.update { it.copy(invoice = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun togglePaidStatus() {
        val current = _uiState.value.invoice ?: return
        viewModelScope.launch {
            val updated = current.copy(isPaid = !current.isPaid)
            invoiceRepository.updateInvoice(updated)
            _uiState.update { it.copy(invoice = updated) }
        }
    }

    fun deleteInvoice(onSuccess: () -> Unit) {
        val current = _uiState.value.invoice ?: return
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(current)
            onSuccess()
        }
    }

    fun sendToKsef(workManager: WorkManager) {
        val invoice = _uiState.value.invoice ?: return

        if (invoice.buyerNip.filter { it.isDigit() }.length != 10) {
            _uiState.update { it.copy(error = "Faktura musi mieć NIP nabywcy (10 cyfr)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingToKsef = true, error = null, ksefMessage = null) }

            val sellerNip  = ksefRepository.authManager.getNip()
            val sellerName = ksefRepository.authManager.companyNameFlow.first().ifBlank { "Sprzedawca" }

            if (sellerNip.isBlank()) {
                _uiState.update {
                    it.copy(
                        isSendingToKsef = false,
                        error = "Najpierw połącz się z KSeF w Ustawienia → Połączenie z KSeF"
                    )
                }
                return@launch
            }

            val xml = KsefXmlGenerator.generate(invoice, sellerNip, sellerName)

            when (val result = ksefRepository.sendInvoice(xml)) {
                is KsefResult.Success -> {
                    val refNumber = result.data
                    val updated = invoice.copy(
                        ksefReferenceNumber = refNumber,
                        ksefStatus = KsefStatus.SENT.name
                    )
                    invoiceRepository.updateInvoice(updated)
                    _uiState.update {
                        it.copy(
                            invoice = updated,
                            isSendingToKsef = false,
                            ksefMessage = "✓ Wysłano! Nr ref: $refNumber"
                        )
                    }
                    scheduleStatusCheck(workManager, invoice.id, refNumber)
                }
                is KsefResult.Error -> {
                    _uiState.update { it.copy(isSendingToKsef = false, error = result.message) }
                }
                else -> _uiState.update { it.copy(isSendingToKsef = false) }
            }
        }
    }

    private fun scheduleStatusCheck(workManager: WorkManager, invoiceId: String, refNumber: String) {
        val inputData = Data.Builder()
            .putString(KsefStatusWorker.KEY_INVOICE_ID, invoiceId)
            .putString(KsefStatusWorker.KEY_KSEF_REF, refNumber)
            .build()

        val request = OneTimeWorkRequestBuilder<KsefStatusWorker>()
            .setInputData(inputData)
            .setInitialDelay(30, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(request)
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, ksefMessage = null) }
    }
}
