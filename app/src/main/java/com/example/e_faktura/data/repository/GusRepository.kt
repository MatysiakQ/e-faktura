package com.example.e_faktura.data.repository

import com.example.e_faktura.data.api.GusService
import com.example.e_faktura.model.GusData
import javax.inject.Inject // <--- Ważne!

// Dodano @Inject constructor
class GusRepository @Inject constructor(
    private val gusService: GusService
) {

    suspend fun searchByNip(nip: String): GusData? {
        return try {
            gusService.getCompanyData(nip)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}