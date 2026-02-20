package com.example.e_faktura.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CompanyType { FIRM, SOLE_PROPRIETORSHIP }

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String = "",
    val nip: String = "",
    val name: String = "",
    val type: CompanyType = CompanyType.FIRM,
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "",
    val userId: String = ""
) {
    val displayName: String
        get() = when (type) {
            CompanyType.FIRM -> name.ifBlank { "Brak nazwy" }
            CompanyType.SOLE_PROPRIETORSHIP -> ownerFullName.ifBlank { "Brak nazwy" }
        }
}