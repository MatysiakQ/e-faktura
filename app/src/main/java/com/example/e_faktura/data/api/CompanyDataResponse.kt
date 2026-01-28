package com.example.e_faktura.data.api

import com.google.gson.annotations.SerializedName

data class CompanyDataResponse(
    @SerializedName("d")
    val result: GusResult?
)

data class GusResult(
    @SerializedName("Regon")
    val regon: String?,

    @SerializedName("Nip")
    val nip: String?,

    @SerializedName("Nazwa")
    val name: String?,

    @SerializedName("Ulica")
    val street: String?,

    @SerializedName("KodPocztowy")
    val zipCode: String?,

    @SerializedName("Miejscowosc")
    val city: String?
)
