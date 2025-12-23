package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: String = "",
    @get:PropertyName("invoice_number") @set:PropertyName("invoice_number") var invoiceNumber: String = "",

    // Dane kontrahenta
    @get:PropertyName("buyer_nip") @set:PropertyName("buyer_nip") var buyerNip: String = "",
    @get:PropertyName("buyer_name") @set:PropertyName("buyer_name") var buyerName: String = "",

    // ROZBICIE KWOT (Zamiast jednego 'amount')
    @get:PropertyName("net_value") @set:PropertyName("net_value") var netValue: Double = 0.0,
    @get:PropertyName("vat_rate") @set:PropertyName("vat_rate") var vatRate: Double = 0.23, // Domyślnie 23%
    @get:PropertyName("vat_value") @set:PropertyName("vat_value") var vatValue: Double = 0.0,
    @get:PropertyName("gross_value") @set:PropertyName("gross_value") var grossValue: Double = 0.0,

    // Daty
    val date: Long = System.currentTimeMillis(), // Data wystawienia
    @get:PropertyName("due_date") @set:PropertyName("due_date") var dueDate: Long = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000, // Domyślnie +14 dni

    // Statusy
    @get:PropertyName("is_paid") @set:PropertyName("is_paid") var isPaid: Boolean = false,

    // Typ: "SALE" (Sprzedaż) lub "PURCHASE" (Zakup/Koszt)
    val type: String = "SALE"
)