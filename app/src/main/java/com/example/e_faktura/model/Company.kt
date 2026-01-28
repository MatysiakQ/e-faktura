package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CompanyType { FIRM, SOLE_PROPRIETORSHIP }

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String = "",
    val nip: String = "",
    val name: String? = null,
    val type: CompanyType = CompanyType.FIRM,
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String? = null,
    val bankAccount: String = "",
    val icon: String = "",
    val userId: String = ""
) {
    //Logika sprawdzana w CompanyTest.kt
    val displayName: String
        get() = when (type) {
            CompanyType.FIRM -> if (!name.isNullOrBlank()) name else "Brak nazwy"
            CompanyType.SOLE_PROPRIETORSHIP -> if (!ownerFullName.isNullOrBlank()) ownerFullName else "Brak nazwy"
        }
}