package com.example.e_faktura.data.api

import com.example.e_faktura.model.GusData
import retrofit2.http.GET
import retrofit2.http.Path

interface GusService {
    @GET("gus/{nip}")
    suspend fun getCompanyData(@Path("nip") nip: String): GusData
}
