package com.example.e_faktura.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_OVERDUE = "overdue_invoices"
        const val CHANNEL_KSEF    = "ksef_status"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_OVERDUE,
                    "Przeterminowane faktury",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_KSEF,
                    "Status KSeF",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    fun showOverdueNotification(invoiceNumber: String) {
        show(
            channelId = CHANNEL_OVERDUE,
            notifId   = invoiceNumber.hashCode(),
            title     = "Faktura przeterminowana!",
            message   = "Faktura $invoiceNumber przekroczyła termin płatności"
        )
    }

    fun showKsefNotification(title: String, message: String) {
        show(
            channelId = CHANNEL_KSEF,
            notifId   = System.currentTimeMillis().toInt(),
            title     = title,
            message   = message
        )
    }

    private fun show(channelId: String, notifId: Int, title: String, message: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(notifId, notification)
    }
}
