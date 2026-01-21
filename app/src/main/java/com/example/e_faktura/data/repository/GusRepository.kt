// Plik: com/example/e_faktura/data/repository/GusRepository.kt
package com.example.e_faktura.data.repository

import com.example.e_faktura.data.api.GusService
import com.example.e_faktura.model.GusData

class GusRepository(private val gusService: GusService) {

    suspend fun searchByNip(nip: String): GusData? {
        return try {
            val response = gusService.getCompanyData(nip)
            // Jeśli używasz Retrofit Response<GusData>, odkomentuj poniższe:
            // if (response.isSuccessful) response.body() else null
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}