package com.example.e_faktura

import kotlinx.serialization.Serializable

@Serializable
data class QrCodeData(
    val businessName: String? = null,
    val ownerFullName: String? = null,
    val nip: String,
    val address: String? = null
)