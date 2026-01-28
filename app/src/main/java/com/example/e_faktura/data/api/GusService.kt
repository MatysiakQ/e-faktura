package com.example.e_faktura.data.api

import com.example.e_faktura.model.GusResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GusService {
    @GET("api/search/nip/{nip}")
    suspend fun getCompanyData(
        @Path("nip") nip: String,
        @Query("date") date: String // format: yyyy-MM-dd
    ): GusResponse
}