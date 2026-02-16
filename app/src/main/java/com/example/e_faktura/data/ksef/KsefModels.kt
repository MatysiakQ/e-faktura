package com.example.e_faktura.data.ksef

import com.google.gson.annotations.SerializedName

// ─── CHALLENGE (krok 1 autoryzacji) ───────────────────────────────────────────
data class KsefChallengeResponse(
    @SerializedName("challenge") val challenge: String = "",
    @SerializedName("timestamp") val timestamp: String = ""
)

// ─── TOKEN (krok 2 autoryzacji) ───────────────────────────────────────────────
data class KsefContextIdentifier(
    @SerializedName("type") val type: String = "onip",
    @SerializedName("identifier") val identifier: String
)

data class KsefTokenRequest(
    @SerializedName("contextIdentifier") val contextIdentifier: KsefContextIdentifier,
    @SerializedName("encryptedToken") val encryptedToken: String
)

data class KsefContextName(
    @SerializedName("type") val type: String = "",
    @SerializedName("tradeName") val tradeName: String? = null,
    @SerializedName("fullName") val fullName: String? = null
)

data class KsefTokenContext(
    @SerializedName("contextIdentifier") val contextIdentifier: KsefContextIdentifier? = null,
    @SerializedName("contextName") val contextName: KsefContextName? = null
)

data class KsefSessionToken(
    @SerializedName("token") val token: String = "",
    @SerializedName("context") val context: KsefTokenContext? = null
)

data class KsefTokenResponse(
    @SerializedName("sessionToken") val sessionToken: KsefSessionToken? = null
)

// ─── KLUCZ PUBLICZNY ──────────────────────────────────────────────────────────
data class KsefPublicKeyResponse(
    @SerializedName("publicKey") val publicKey: String = ""
)

// ─── WYSYŁKA FAKTURY ──────────────────────────────────────────────────────────
data class KsefInvoicePayload(
    @SerializedName("type") val type: String = "plain",
    @SerializedName("invoiceBody") val invoiceBody: String,
    @SerializedName("encryptedCredentialsKeyForSessionToken") val encryptedCredentials: String? = null
)

data class KsefSendInvoiceRequest(
    @SerializedName("invoicePayload") val invoicePayload: KsefInvoicePayload
)

data class KsefSendInvoiceResponse(
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("processingCode") val processingCode: Int = 0,
    @SerializedName("processingDescription") val processingDescription: String = "",
    @SerializedName("timestamp") val timestamp: String = ""
)

// ─── STATUS FAKTURY ───────────────────────────────────────────────────────────
data class KsefInvoiceStatusDetail(
    @SerializedName("invoiceStatus") val status: Int = 0,
    @SerializedName("ksefReferenceNumber") val ksefReferenceNumber: String = "",
    @SerializedName("acquisitionTimestamp") val acquisitionTimestamp: String = ""
)

data class KsefInvoiceStatusResponse(
    @SerializedName("processingCode") val processingCode: Int = 0,
    @SerializedName("processingDescription") val processingDescription: String = "",
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("invoiceStatus") val invoiceStatus: KsefInvoiceStatusDetail? = null
)

// ─── POBIERANIE FAKTUR ────────────────────────────────────────────────────────
data class KsefQueryCriteria(
    @SerializedName("type") val type: String = "incremental",
    @SerializedName("acquisitionTimestampThresholdFrom") val from: String? = null,
    @SerializedName("acquisitionTimestampThresholdTo") val to: String? = null
)

data class KsefDownloadRequest(
    @SerializedName("queryCriteria") val queryCriteria: KsefQueryCriteria
)

data class KsefDownloadResponse(
    @SerializedName("timestamp") val timestamp: String = "",
    @SerializedName("referenceNumber") val referenceNumber: String = ""
)

// ─── WYNIK OPERACJI (sealed class dla ViewModel) ──────────────────────────────
sealed class KsefResult<out T> {
    data class Success<T>(val data: T) : KsefResult<T>()
    data class Error(val message: String, val code: Int? = null) : KsefResult<Nothing>()
    object Loading : KsefResult<Nothing>()
}
