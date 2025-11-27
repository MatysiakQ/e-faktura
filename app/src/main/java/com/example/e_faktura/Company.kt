package com.example.e_faktura

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

enum class IconType {
    PREDEFINED,
    CUSTOM
}

@Serializable
@Parcelize
data class CompanyIcon(
    val type: IconType = IconType.PREDEFINED,
    val iconName: String = "Business"
) : Parcelable

@Serializable
@Parcelize
data class Company(
    val type: CompanyType = CompanyType.FIRM,
    val nip: String = "",
    val address: String = "",
    val ownerFullName: String? = null,
    val businessName: String? = null,
    val icon: CompanyIcon = CompanyIcon(IconType.PREDEFINED, "Business")
) : Parcelable {
    val displayName: String
        get() = when (type) {
            CompanyType.FIRM -> businessName ?: "Brak nazwy"
            CompanyType.SOLE_PROPRIETORSHIP -> ownerFullName ?: "Brak nazwy"
        }
}
