package com.example.e_faktura.data.repository

import com.example.e_faktura.data.api.GusService
import com.example.e_faktura.model.GusData
import java.text.SimpleDateFormat
import java.util.*

class GusRepository(private val gusService: GusService) {

    suspend fun searchByNip(nip: String): GusData? {
        val cleanNip = nip.replace("-", "").replace(" ", "").trim()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return try {
            val response = gusService.getCompanyData(cleanNip, today)
            val subject = response.result?.subject ?: return null

            val fullAddress = subject.address ?: ""

            // REGEX: Rozbija adres typu "ul. Prosta 49 00-838 Warszawa"
            // Group 1: Ulica i numer, Group 2: Kod pocztowy, Group 3: Miasto
            val regex = """^(.*)\s(\d{2}-\d{3})\s(.*)$""".toRegex()
            val match = regex.find(fullAddress)

            GusData(
                name = subject.name,
                address = match?.groupValues?.get(1)?.trim() ?: fullAddress,
                postalCode = match?.groupValues?.get(2) ?: "",
                city = match?.groupValues?.get(3)?.trim() ?: "",
                bankAccount = subject.accountNumbers?.firstOrNull() // Pobieramy pierwsze konto
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}