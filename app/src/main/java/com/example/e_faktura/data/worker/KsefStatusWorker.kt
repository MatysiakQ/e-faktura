package com.example.e_faktura.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.e_faktura.EfakturaApplication
import com.example.e_faktura.data.ksef.KsefResult
import com.example.e_faktura.model.KsefStatus
import com.example.e_faktura.utils.NotificationHelper

class KsefStatusWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_INVOICE_ID = "invoice_id"
        const val KEY_KSEF_REF   = "ksef_reference_number"
    }

    override suspend fun doWork(): Result {
        val invoiceId = inputData.getString(KEY_INVOICE_ID) ?: return Result.failure()
        val ksefRef   = inputData.getString(KEY_KSEF_REF)   ?: return Result.failure()

        val container          = (applicationContext as EfakturaApplication).container
        val ksefRepo           = container.ksefRepository
        val invoiceRepo        = container.invoiceRepository
        val notificationHelper = NotificationHelper(applicationContext)

        return try {
            when (val result = ksefRepo.checkInvoiceStatus(ksefRef)) {
                is KsefResult.Success -> {
                    val code = result.data.processingCode
                    val newStatus = when {
                        code == 200  -> KsefStatus.ACCEPTED
                        code >= 400  -> KsefStatus.REJECTED
                        else         -> KsefStatus.SENT
                    }

                    val invoice = invoiceRepo.getInvoiceById(invoiceId)
                    invoice?.let {
                        invoiceRepo.updateInvoice(it.copy(ksefStatus = newStatus.name))
                    }

                    when (newStatus) {
                        KsefStatus.ACCEPTED ->
                            notificationHelper.showKsefNotification(
                                "Faktura zaakceptowana ✓",
                                "Faktura ${invoice?.invoiceNumber} przyjęta przez KSeF"
                            )
                        KsefStatus.REJECTED ->
                            notificationHelper.showKsefNotification(
                                "Faktura odrzucona ✗",
                                "Faktura ${invoice?.invoiceNumber} odrzucona przez KSeF"
                            )
                        else -> { /* nadal procesowana */ }
                    }

                    if (newStatus == KsefStatus.SENT) Result.retry() else Result.success()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
