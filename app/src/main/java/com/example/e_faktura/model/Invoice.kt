package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

// Status płatności faktury
enum class InvoiceStatus {
    PAID,       // Opłacona
    PENDING,    // Oczekująca (w terminie)
    OVERDUE     // Przedawniona (po terminie)
}

// Status faktury w systemie KSeF
enum class KsefStatus {
    LOCAL,      // Tylko lokalna, nie wysłana do KSeF
    SENDING,    // W trakcie wysyłania
    SENT,       // Wysłana, oczekuje na weryfikację
    ACCEPTED,   // Zaakceptowana przez KSeF
    REJECTED    // Odrzucona przez KSeF
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey
    val id: String = "",
    val invoiceNumber: String = "",
    val type: String = "PRZYCHOD",

    // --- Kwoty ---
    val netAmount: Double = 0.0,
    val vatRate: String = "23",
    val vatAmount: Double = 0.0,
    val grossAmount: Double = 0.0,
    // Zachowane dla wstecznej kompatybilności
    val amount: Double = 0.0,

    // --- Nabywca ---
    val buyerName: String = "",
    val buyerNip: String = "",

    // --- Sprzedawca ---
    val sellerName: String = "",
    val sellerNip: String = "",

    // --- Szczegóły faktury ---
    val serviceDescription: String = "",
    val paymentMethod: String = "PRZELEW",
    val invoiceDate: Long = 0L,
    val dueDate: Long = 0L,

    // --- Integracja KSeF ---
    val ksefReferenceNumber: String = "",
    val ksefStatus: String = KsefStatus.LOCAL.name,

    @get:PropertyName("isPaid")
    @set:PropertyName("isPaid")
    var isPaid: Boolean = false,

    val userId: String = ""
)

fun Invoice.getStatus(): InvoiceStatus = when {
    isPaid -> InvoiceStatus.PAID
    dueDate > 0 && dueDate < System.currentTimeMillis() -> InvoiceStatus.OVERDUE
    else -> InvoiceStatus.PENDING
}

fun Invoice.getKsefStatus(): KsefStatus = try {
    KsefStatus.valueOf(ksefStatus)
} catch (e: IllegalArgumentException) {
    KsefStatus.LOCAL
}

fun calculateVat(netAmount: Double, vatRate: String): Double = when (vatRate) {
    "ZW", "0" -> 0.0
    else -> netAmount * (vatRate.toDoubleOrNull() ?: 0.0) / 100.0
}
