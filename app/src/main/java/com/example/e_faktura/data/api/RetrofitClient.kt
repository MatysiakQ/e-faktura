package com.example.e_faktura.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //Oficjalne API Ministerstwa Finansów (Biała Lista)
    private const val BASE_URL = "https://wl-api.mf.gov.pl/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val gusService: GusService by lazy {
        retrofit.create(GusService::class.java)
    }
}