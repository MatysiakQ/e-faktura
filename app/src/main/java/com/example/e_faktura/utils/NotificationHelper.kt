package com.example.e_faktura.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.e_faktura.R

class NotificationHelper(private val context: Context) {
    private val channelId = "invoice_alerts"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Powiadomienia o fakturach",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Informuje o przedawnionych fakturach"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOverdueNotification(invoiceNumber: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Przedawniona faktura!")
            .setContentText("Faktura NR $invoiceNumber przedawniła się, zweryfikuj czy usługodawca opłacił ją i odznacz to w aplikacji!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Faktura NR $invoiceNumber przedawniła się, zweryfikuj czy usługodawca opłacił ją i odznacz to w aplikacji!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(invoiceNumber.hashCode(), notification)
    }
}