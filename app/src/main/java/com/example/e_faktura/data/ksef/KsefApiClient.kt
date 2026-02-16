package com.example.e_faktura.data.ksef

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Klient Retrofit dla KSeF API.
 * Obsługuje dwa środowiska: testowe i produkcyjne.
 *
 * WAŻNE: Używaj środowiska testowego (domyślne) do czasu pełnego wdrożenia.
 * Przestaw `isProduction = true` dopiero w wersji produkcyjnej apki.
 */
object KsefApiClient {

    private const val BASE_URL_TEST = "https://ksef-test.mf.gov.pl/api/v2/"
    private const val BASE_URL_PROD = "https://ksef.mf.gov.pl/api/v2/"

    /** Flaga środowiska — domyślnie testowe */
    var isProduction: Boolean = false

    private fun buildOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Dodaj nagłówki do każdego żądania
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)  // KSeF może być wolne przy dużym ruchu
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(buildOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Serwis dla środowiska testowego */
    val testService: KsefApiService by lazy {
        buildRetrofit(BASE_URL_TEST).create(KsefApiService::class.java)
    }

    /** Serwis dla środowiska produkcyjnego */
    val prodService: KsefApiService by lazy {
        buildRetrofit(BASE_URL_PROD).create(KsefApiService::class.java)
    }

    /** Aktywny serwis — zależny od flagi isProduction */
    val service: KsefApiService get() = if (isProduction) prodService else testService
}
