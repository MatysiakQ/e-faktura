package com.example.e_faktura.data.api

import com.google.gson.annotations.SerializedName

/**
 * CANONICAL Data Transfer Object (DTO) for the GUS API response.
 * This class directly maps to the JSON structure returned by the service.
 */
data class CompanyDataResponse(
    @SerializedName("d")
    val result: GusResult? // The actual data is nested under the 'd' key
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
