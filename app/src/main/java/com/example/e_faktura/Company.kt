package com.example.e_faktura

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class IconType {
    PREDEFINED,
    CUSTOM
}

enum class CompanyType {
    FIRM,
    SOLE_PROPRIETORSHIP
}

@Parcelize
data class CompanyIcon(
    val type: IconType = IconType.PREDEFINED,
    val iconName: String = "Business"
) : Parcelable {
    constructor() : this(IconType.PREDEFINED, "Business")
}

@Parcelize
data class Company(
    val type: CompanyType = CompanyType.FIRM,
    val nip: String = "",
    val address: String = "",
    val ownerFullName: String? = null,
    val businessName: String? = null,
    val icon: CompanyIcon = CompanyIcon()
) : Parcelable {
    constructor() : this(CompanyType.FIRM, "", "", null, null, CompanyIcon())

    val displayName: String
        get() = when (type) {
            CompanyType.FIRM -> businessName ?: "Brak nazwy"
            CompanyType.SOLE_PROPRIETORSHIP -> ownerFullName ?: "Brak nazwy"
        }
}