package com.example.e_faktura.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.e_faktura.EfakturaApplication
import com.example.e_faktura.model.Invoice
import com.example.e_faktura.model.InvoiceStatus // ✅
import com.example.e_faktura.model.getStatus    // ✅
import com.example.e_faktura.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class OverdueInvoiceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as EfakturaApplication).container
        val notificationHelper = NotificationHelper(applicationContext)

        return try {
            val invoices: List<Invoice> = container.invoiceRepository.getInvoices().first()

            // ✅ Korzystamy z centralnej logiki statusu
            val overdueInvoices = invoices.filter { it.getStatus() == InvoiceStatus.OVERDUE }

            overdueInvoices.forEach { invoice ->
                notificationHelper.showOverdueNotification(invoice.invoiceNumber)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}