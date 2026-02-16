package com.example.e_faktura.data.ksef

import android.util.Base64
import kotlinx.coroutines.flow.Flow

/**
 * Repozytorium operacji KSeF.
 * Łączy KsefApiService, KsefAuthManager i KsefEncryptionHelper
 * w jeden spójny interfejs dla ViewModelu.
 */
class KsefRepository(
    private val apiService: KsefApiService,
    val authManager: KsefAuthManager
) {

    // ─── Flows (przekazywane z AuthManager) ──────────────────────────────────
    val isConnected: Flow<Boolean> = authManager.isConnectedFlow
    val nipFlow: Flow<String> = authManager.nipFlow
    val companyNameFlow: Flow<String> = authManager.companyNameFlow
    val isProductionFlow: Flow<Boolean> = authManager.isProductionFlow

    // ─── Diagnostyka ─────────────────────────────────────────────────────────

    /** Sprawdź czy KSeF API jest dostępne */
    suspend fun checkApiHealth(): KsefResult<Boolean> = safeApiCall {
        val response = apiService.checkHealth()
        if (response.isSuccessful) KsefResult.Success(true)
        else KsefResult.Error("API niedostępne (kod: ${response.code()})", response.code())
    }

    // ─── Autoryzacja ─────────────────────────────────────────────────────────

    /**
     * Kompletny flow autoryzacji KSeF:
     * 1. Pobiera klucz publiczny serwera
     * 2. Pobiera challenge dla NIP
     * 3. Szyfruje token KSeF kluczem publicznym
     * 4. Wymienia challenge na token sesji JWT
     * 5. Zapisuje token sesji
     */
    suspend fun authorize(nip: String, ksefToken: String, companyName: String): KsefResult<String> =
        safeApiCall {
            // Krok 1: Klucz publiczny
            val pkResponse = apiService.getPublicKey()
            if (!pkResponse.isSuccessful)
                return@safeApiCall KsefResult.Error("Błąd pobierania klucza publicznego (${pkResponse.code()})")
            val publicKey = pkResponse.body()?.publicKey
                ?: return@safeApiCall KsefResult.Error("Serwer nie zwrócił klucza publicznego")

            // Krok 2: Challenge
            val challengeResponse = apiService.getChallenge(nip)
            if (!challengeResponse.isSuccessful)
                return@safeApiCall KsefResult.Error("Błąd challenge (${challengeResponse.code()})")
            val challenge = challengeResponse.body()?.challenge
                ?: return@safeApiCall KsefResult.Error("Serwer nie zwrócił challenge")

            // Krok 3: Szyfrowanie tokenu
            val encryptedToken = KsefEncryptionHelper.encryptTokenWithPublicKey(ksefToken, publicKey)

            // Krok 4: Generowanie sesji
            val tokenRequest = KsefTokenRequest(
                contextIdentifier = KsefContextIdentifier(identifier = nip),
                encryptedToken = encryptedToken
            )
            val tokenResponse = apiService.generateToken(tokenRequest)
            if (!tokenResponse.isSuccessful)
                return@safeApiCall KsefResult.Error("Błąd generowania sesji (${tokenResponse.code()})")
            val sessionToken = tokenResponse.body()?.sessionToken?.token
                ?: return@safeApiCall KsefResult.Error("Serwer nie zwrócił tokenu sesji")

            // Krok 5: Zapis
            authManager.saveCredentials(nip, ksefToken, companyName)
            authManager.saveSessionToken(sessionToken)

            KsefResult.Success(sessionToken)
        }

    /** Rozłącz bieżącą sesję */
    suspend fun disconnect() = authManager.disconnectSession()

    /** Usuń wszystkie dane KSeF */
    suspend fun clearAllData() = authManager.clearAll()

    /** Zmień środowisko */
    suspend fun setEnvironment(isProduction: Boolean) = authManager.setEnvironment(isProduction)

    // ─── Faktury ──────────────────────────────────────────────────────────────

    /**
     * Wyślij fakturę do KSeF (tryb plain — bez szyfrowania, dla środowiska testowego).
     * W Fazie 2 dodamy tryb encrypted z KsefEncryptionHelper.
     *
     * @param xmlContent Surowy XML faktury FA(3)
     * @return Numer referencyjny KSeF lub błąd
     */
    suspend fun sendInvoice(xmlContent: String): KsefResult<String> = safeApiCall {
        val sessionToken = authManager.getSessionToken()
            ?: return@safeApiCall KsefResult.Error("Brak aktywnej sesji. Połącz się z KSeF w Ustawieniach.")

        val invoiceBody = Base64.encodeToString(
            xmlContent.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )

        val request = KsefSendInvoiceRequest(
            invoicePayload = KsefInvoicePayload(
                type = "plain",
                invoiceBody = invoiceBody
            )
        )

        val response = apiService.sendInvoice("Bearer $sessionToken", request)
        if (response.isSuccessful) {
            KsefResult.Success(response.body()?.referenceNumber ?: "")
        } else {
            KsefResult.Error("Błąd wysyłki (${response.code()}): ${response.message()}", response.code())
        }
    }

    /**
     * Sprawdź status wysłanej faktury.
     * @param referenceNumber Numer referencyjny zwrócony przez KSeF po wysłaniu
     */
    suspend fun checkInvoiceStatus(referenceNumber: String): KsefResult<KsefInvoiceStatusResponse> =
        safeApiCall {
            val sessionToken = authManager.getSessionToken()
                ?: return@safeApiCall KsefResult.Error("Brak aktywnej sesji KSeF")

            val response = apiService.getInvoiceStatus("Bearer $sessionToken", referenceNumber)
            if (response.isSuccessful) {
                KsefResult.Success(response.body()!!)
            } else {
                KsefResult.Error("Błąd statusu (${response.code()})", response.code())
            }
        }

    // ─── Pomocnik ─────────────────────────────────────────────────────────────

    /** Opakowuje wywołanie API w try-catch → KsefResult */
    private suspend fun <T> safeApiCall(block: suspend () -> KsefResult<T>): KsefResult<T> = try {
        block()
    } catch (e: Exception) {
        KsefResult.Error("Błąd połączenia: ${e.localizedMessage ?: "Nieznany błąd"}")
    }
}
