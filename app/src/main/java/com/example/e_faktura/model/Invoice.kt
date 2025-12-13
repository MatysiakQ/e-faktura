package com.example.e_faktura.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Invoice(
    val id: String = "",
    // ADDED: The missing invoice number field
    val invoiceNumber: String = "",
    val sellerId: String = "",
    val buyerName: String = "",
    val buyerNip: String = "",
    val amount: Double = 0.0,
    val date: Long = 0L,
    val isPaid: Boolean = false
) : Parcelable
