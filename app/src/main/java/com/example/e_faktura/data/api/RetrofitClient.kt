package com.example.e_faktura.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Adres API do pobierania danych z GUS
    private const val BASE_URL = "https://twoje-api-gus.pl/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ To jest serwis, o który wołał AppContainer
    val gusService: GusService by lazy {
        retrofit.create(GusService::class.java)
    }
}