package com.example.e_faktura.data.api

import com.example.e_faktura.model.GusData
import retrofit2.http.GET
import retrofit2.http.Query

interface GusService {
    // Przykładowy endpoint. W prawdziwym GUS (BIR1) jest to dużo bardziej skomplikowane.
    // Tutaj zakładamy, że masz jakieś proste API lub wrapper.
    @GET("search")
    suspend fun getCompanyData(@Query("nip") nip: String): GusData?
}