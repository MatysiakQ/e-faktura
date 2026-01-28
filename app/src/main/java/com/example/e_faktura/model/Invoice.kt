package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

// ✅ DODANO: Logika statusów faktury
enum class InvoiceStatus {
    PAID,       // Opłacona
    PENDING,    // Oczekująca (w terminie)
    OVERDUE     // Przedawniona (po terminie)
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey
    val id: String = "",
    val invoiceNumber: String = "",
    val type: String = "PRZYCHOD",
    val amount: Double = 0.0,
    val buyerName: String = "",
    val buyerNip: String = "",
    val dueDate: Long = 0L,

    @get:PropertyName("isPaid")
    @set:PropertyName("isPaid")
    var isPaid: Boolean = false,

    val userId: String = ""
)

// ✅ DODANO: Rozszerzenie ułatwiające sprawdzanie statusu w UI i Workerze
fun Invoice.getStatus(): InvoiceStatus {
    return when {
        isPaid -> InvoiceStatus.PAID
        dueDate < System.currentTimeMillis() -> InvoiceStatus.OVERDUE
        else -> InvoiceStatus.PENDING
    }
}