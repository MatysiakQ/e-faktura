package com.example.e_faktura.ui.company.add

import java.util.UUID

data class CompanyUiState(
    val id: String = UUID.randomUUID().toString(),
    val nip: String = "",
    val name: String = "",
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val icon: String = "VECTOR:Storefront",
    val isLoading: Boolean = false,
    val error: String? = null // Dodano pole błędu
)

sealed class UiEvent {
    object SaveSuccess : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}