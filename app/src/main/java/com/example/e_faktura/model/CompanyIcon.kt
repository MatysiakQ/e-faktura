package com.example.e_faktura.model

enum class IconType {
    PREDEFINED,
    CUSTOM
}

data class CompanyIcon(val type: IconType, val value: String)