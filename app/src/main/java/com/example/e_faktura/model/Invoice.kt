package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey
    val id: String = "",
    val invoiceNumber: String = "",
    val type: String = "PRZYCHOD", // PRZYCHOD lub KOSZT
    val amount: Double = 0.0,
    val buyerName: String = "",
    val buyerNip: String = "",      // ✅ DODANO: Tego brakowało w modelu
    val dueDate: Long = 0L,         // ✅ Używamy dueDate zamiast date (timestamp)
    val isPaid: Boolean = false,
    val userId: String = ""
)