package com.example.e_faktura

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Company(
    val type: CompanyType,
    val nip: String,
    val address: String,
    val ownerFullName: String? = null,
    val businessName: String? = null
) : Parcelable {
    val displayName: String
        get() = when (type) {
            CompanyType.FIRM -> businessName ?: "Brak nazwy"
            CompanyType.SOLE_PROPRIETORSHIP -> ownerFullName ?: "Brak nazwy"
        }
}
