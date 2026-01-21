// Plik: com/example/e_faktura/model/Company.kt
package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String = "",
    val nip: String = "",
    val name: String = "",          // Musi być 'name', nie 'companyName'
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "",
    val userId: String = ""
)