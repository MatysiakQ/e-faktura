package com.example.e_faktura.data.ksef

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Helper do szyfrowania dla KSeF API 2.0.
 *
 * KSeF wymaga szyfrowania na dwóch poziomach:
 * 1. Token autoryzacyjny → RSAES-OAEP (SHA-256, MGF1) kluczem publicznym serwera
 * 2. Faktura XML → AES-256-CBC (klucz losowy), klucz AES → RSAES-OAEP kluczem publicznym
 */
object KsefEncryptionHelper {

    private const val RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private const val AES_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val AES_KEY_SIZE = 256
    private const val IV_SIZE = 16

    /**
     * Szyfruje token KSeF kluczem publicznym serwera.
     * Używane w kroku 1 autoryzacji (challenge → token).
     *
     * @param token Token KSeF użytkownika (z portalu podatki.gov.pl)
     * @param publicKeyBase64 Klucz publiczny KSeF zakodowany w Base64
     * @return Zaszyfrowany token zakodowany w Base64
     */
    fun encryptTokenWithPublicKey(token: String, publicKeyBase64: String): String {
        val publicKey = decodePublicKey(publicKeyBase64)
        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    /**
     * Szyfruje XML faktury dla KSeF (tryb szyfrowany).
     * Generuje losowy klucz AES, szyfruje nim XML, a następnie
     * szyfruje klucz AES kluczem publicznym serwera.
     *
     * @param xmlContent Surowy XML faktury w formacie FA(3)
     * @param publicKeyBase64 Klucz publiczny KSeF zakodowany w Base64
     * @return Obiekt zawierający zaszyfrowaną treść, klucz i IV
     */
    fun encryptInvoiceXml(xmlContent: String, publicKeyBase64: String): EncryptedInvoiceData {
        // 1. Generuj losowy klucz AES-256
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE, SecureRandom())
        val aesKey: SecretKey = keyGenerator.generateKey()

        // 2. Generuj losowy IV (Initialization Vector)
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val ivSpec = IvParameterSpec(iv)

        // 3. Szyfruj XML kluczem AES-256-CBC
        val aesCipher = Cipher.getInstance(AES_ALGORITHM)
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec)
        val encryptedXml = aesCipher.doFinal(xmlContent.toByteArray(Charsets.UTF_8))

        // 4. Szyfruj klucz AES kluczem publicznym serwera (RSAES-OAEP)
        val publicKey = decodePublicKey(publicKeyBase64)
        val rsaCipher = Cipher.getInstance(RSA_ALGORITHM)
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedAesKey = rsaCipher.doFinal(aesKey.encoded)

        return EncryptedInvoiceData(
            encryptedContent = Base64.encodeToString(encryptedXml, Base64.NO_WRAP),
            encryptedKey = Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    private fun decodePublicKey(base64Key: String): PublicKey {
        val keyBytes = Base64.decode(base64Key.trim(), Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }
}

/** Wynik szyfrowania faktury */
data class EncryptedInvoiceData(
    val encryptedContent: String,   // Zaszyfrowany XML (Base64)
    val encryptedKey: String,       // Zaszyfrowany klucz AES (Base64)
    val iv: String                  // Initialization Vector (Base64)
)
