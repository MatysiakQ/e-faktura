package com.example.e_faktura.data.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class SyncService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SyncService", "Usługa synchronizacji uruchomiona")
        serviceScope.launch {
            delay(5000)
            Log.d("SyncService", "Synchronizacja zakończona")
            stopSelf()
        }
        return START_NOT_STICKY
    }
}