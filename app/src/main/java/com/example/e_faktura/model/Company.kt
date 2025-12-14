package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String = "",
    val businessName: String = "",
    val nip: String = "",
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "PREDEFINED:Business",
    val userId: String = "",

    @ServerTimestamp
    val createdAt: Date = Date()
)
