package com.example.e_faktura.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * CANONICAL Retrofit service interface for the GUS (Główny Urząd Statystyczny) API.
 * All GUS-related network calls MUST be defined here.
 */
interface GusService {

    /**
     * Searches for a company entity by its NIP number.
     * The query parameter is structured specifically for the GUS API.
     * @param nipQuery The formatted query string containing the NIP number.
     * @return A [CompanyDataResponse] DTO object.
     */
    @GET("DaneSzukajPodmioty")
    suspend fun searchByNip(
        @Query("pParametryWyszukiwania") nipQuery: String
    ): CompanyDataResponse
}
