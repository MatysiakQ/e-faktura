package com.example.e_faktura.data.ksef

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfejs Retrofit dla KSeF API v2.
 * Dokumentacja: https://ksef.podatki.gov.pl/ksef-na-okres-obligatoryjny/wsparcie-dla-integratorow
 */
interface KsefApiService {

    /** Sprawdzenie dostępności API — brak autoryzacji */
    @GET("health")
    suspend fun checkHealth(): Response<Unit>

    /** Pobieranie klucza publicznego serwera — wymagane do szyfrowania */
    @GET("security/public-key-certificates")
    suspend fun getPublicKey(): Response<KsefPublicKeyResponse>

    /** Krok 1 autoryzacji: pobierz challenge dla danego NIP */
    @POST("auth/challenge/nip/{nip}")
    suspend fun getChallenge(
        @Path("nip") nip: String
    ): Response<KsefChallengeResponse>

    /** Krok 2 autoryzacji: wymień zaszyfrowany token na sesję JWT */
    @POST("auth/token/generate")
    suspend fun generateToken(
        @Body request: KsefTokenRequest
    ): Response<KsefTokenResponse>

    /** Wyślij fakturę do KSeF — wymaga aktywnej sesji */
    @POST("invoices/send")
    suspend fun sendInvoice(
        @Header("Authorization") token: String,
        @Body request: KsefSendInvoiceRequest
    ): Response<KsefSendInvoiceResponse>

    /** Sprawdź status wysłanej faktury */
    @GET("invoices/status/{referenceNumber}")
    suspend fun getInvoiceStatus(
        @Header("Authorization") token: String,
        @Path("referenceNumber") referenceNumber: String
    ): Response<KsefInvoiceStatusResponse>

    /** Inicjuj pobieranie faktur przychodzących */
    @POST("invoices/download/request")
    suspend fun requestInvoicesDownload(
        @Header("Authorization") token: String,
        @Body request: KsefDownloadRequest
    ): Response<KsefDownloadResponse>
}
