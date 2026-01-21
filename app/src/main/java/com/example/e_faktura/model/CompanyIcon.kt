package com.example.e_faktura.model

// To jest serce problemu - musimy mieć VECTOR i CUSTOM
enum class IconType {
    VECTOR, // Dla ikon systemowych (np. Business, Search)
    CUSTOM  // Dla zdjęć wybranych z galerii użytkownika
}

data class CompanyIcon(
    val type: IconType,
    val value: String // Tutaj trafia nazwa ikony (np. "Business") lub URI zdjęcia
)