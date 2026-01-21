package com.example.e_faktura.data.api

import com.example.e_faktura.model.GusData
import retrofit2.http.GET
import retrofit2.http.Query

interface GusService {
    // Ścieżka zależy od Twojego konkretnego API GUS/REGON
    @GET("api/query/nip")
    suspend fun getCompanyData(@Query("nip") nip: String): GusData
}