package com.example.e_faktura.model

enum class IconType {
    VECTOR,
    CUSTOM
}

data class CompanyIcon(
    val type: IconType,
    val value: String
)