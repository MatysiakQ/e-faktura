package com.example.e_faktura

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.e_faktura.data.AppContainer
import com.example.e_faktura.data.DefaultAppContainer
import com.example.e_faktura.data.worker.OverdueInvoiceWorker
import java.util.concurrent.TimeUnit

class EfakturaApplication : Application() {
    /**
     * Instancja AppContainer używana przez resztę klas do uzyskiwania zależności.
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // ✅ Inicjalizacja Twojego kontenera
        container = DefaultAppContainer(this)

        // ✅ Uruchomienie sprawdzania faktur w tle
        setupOverdueWorker()
    }

    private fun setupOverdueWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Działa nawet bez internetu (baza lokalna)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<OverdueInvoiceWorker>(
            12, TimeUnit.HOURS // Sprawdzaj terminy co 12 godzin
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "OverdueInvoiceCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}