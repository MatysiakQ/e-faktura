package com.example.e_faktura.data.ksef

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.e_faktura.ui.settings.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Zarządza danymi autoryzacji KSeF.
 * Przechowuje NIP firmy, token KSeF i token sesji w DataStore.
 *
 * UWAGA: W przyszłości warto zamienić DataStore na EncryptedSharedPreferences
 * dla dodatkowego bezpieczeństwa danych wrażliwych (tokenu KSeF).
 */
class KsefAuthManager(private val context: Context) {

    companion object {
        private val KEY_NIP            = stringPreferencesKey("ksef_nip")
        private val KEY_KSEF_TOKEN     = stringPreferencesKey("ksef_token")
        private val KEY_SESSION_TOKEN  = stringPreferencesKey("ksef_session_token")
        private val KEY_IS_CONNECTED   = booleanPreferencesKey("ksef_is_connected")
        private val KEY_IS_PRODUCTION  = booleanPreferencesKey("ksef_is_production")
        private val KEY_COMPANY_NAME   = stringPreferencesKey("ksef_company_name")
    }

    // ─── Flows (obserwowalne wartości) ────────────────────────────────────────
    val nipFlow: Flow<String> =
        context.dataStore.data.map { it[KEY_NIP] ?: "" }

    val ksefTokenFlow: Flow<String> =
        context.dataStore.data.map { it[KEY_KSEF_TOKEN] ?: "" }

    val sessionTokenFlow: Flow<String> =
        context.dataStore.data.map { it[KEY_SESSION_TOKEN] ?: "" }

    val isConnectedFlow: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_IS_CONNECTED] ?: false }

    val isProductionFlow: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_IS_PRODUCTION] ?: false }

    val companyNameFlow: Flow<String> =
        context.dataStore.data.map { it[KEY_COMPANY_NAME] ?: "" }

    // ─── Operacje zapisu ─────────────────────────────────────────────────────

    /** Zapisz dane logowania (NIP + token KSeF) */
    suspend fun saveCredentials(nip: String, ksefToken: String, companyName: String = "") {
        context.dataStore.edit { prefs ->
            prefs[KEY_NIP] = nip.trim()
            prefs[KEY_KSEF_TOKEN] = ksefToken.trim()
            prefs[KEY_COMPANY_NAME] = companyName
        }
    }

    /** Zapisz token sesji po pomyślnej autoryzacji */
    suspend fun saveSessionToken(sessionToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SESSION_TOKEN] = sessionToken
            prefs[KEY_IS_CONNECTED] = sessionToken.isNotBlank()
        }
    }

    /** Zmień środowisko (testowe/produkcyjne) */
    suspend fun setEnvironment(isProduction: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PRODUCTION] = isProduction
        }
        KsefApiClient.isProduction = isProduction
    }

    /** Odśwież środowisko z DataStore przy starcie apki */
    suspend fun syncEnvironmentFromPrefs() {
        val isProd = context.dataStore.data.first()[KEY_IS_PRODUCTION] ?: false
        KsefApiClient.isProduction = isProd
    }

    /** Rozłącz sesję (nie usuwa credentials) */
    suspend fun disconnectSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SESSION_TOKEN] = ""
            prefs[KEY_IS_CONNECTED] = false
        }
    }

    /** Usuń wszystkie dane KSeF */
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs[KEY_NIP] = ""
            prefs[KEY_KSEF_TOKEN] = ""
            prefs[KEY_SESSION_TOKEN] = ""
            prefs[KEY_IS_CONNECTED] = false
            prefs[KEY_COMPANY_NAME] = ""
        }
    }

    // ─── Odczyt synchroniczny (jednorazowy) ──────────────────────────────────

    suspend fun getSessionToken(): String? =
        context.dataStore.data.first()[KEY_SESSION_TOKEN]?.ifBlank { null }

    suspend fun getNip(): String =
        context.dataStore.data.first()[KEY_NIP] ?: ""

    suspend fun getKsefToken(): String =
        context.dataStore.data.first()[KEY_KSEF_TOKEN] ?: ""
}
