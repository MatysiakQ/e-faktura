package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: String = "",
    @get:PropertyName("invoice_number") @set:PropertyName("invoice_number") var invoiceNumber: String = "",
    @get:PropertyName("buyer_nip") @set:PropertyName("buyer_nip") var buyerNip: String = "",
    @get:PropertyName("buyer_name") @set:PropertyName("buyer_name") var buyerName: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    @get:PropertyName("due_date") @set:PropertyName("due_date") var dueDate: Long = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000, // Default 14 days
    @get:PropertyName("is_paid") @set:PropertyName("is_paid") var isPaid: Boolean = false,
    val type: String = "SALE" // Default to SALE
)
