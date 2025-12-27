package com.example.e_faktura.model

import com.google.gson.annotations.SerializedName

data class GusData(
    @SerializedName("name")
    val name: String?,

    @SerializedName("address")
    val address: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("postalCode")
    val postalCode: String?,

    @SerializedName("nip")
    val nip: String?
)