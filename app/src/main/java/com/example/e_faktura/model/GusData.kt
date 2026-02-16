package com.example.e_faktura.model

import com.google.gson.annotations.SerializedName

data class GusResponse(
    @SerializedName("result") val result: GusResult? = null
)

data class GusResult(
    @SerializedName("subject") val subject: GusSubject? = null
)

data class GusSubject(
    @SerializedName("name") val name: String? = null,
    @SerializedName("nip") val nip: String? = null,
    @SerializedName("workingAddress") val workingAddress: String? = null,
    @SerializedName("residenceAddress") val residenceAddress: String? = null,
    @SerializedName("accountNumbers") val accountNumbers: List<String>? = null
)

data class GusData(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val bankAccount: String? = null
)