package com.example.e_faktura.data.repository

import com.example.e_faktura.data.api.GusService // <-- CORRECTED IMPORT
import com.example.e_faktura.model.GusData

/**
 * Repository for fetching company data from the GUS (Polish Central Statistical Office) API.
 */
class GusRepository(private val gusService: GusService) {

    /**
     * Searches for company data by NIP.
     * In a real implementation, this would handle network errors and mapping.
     */
    suspend fun searchByNip(nip: String): GusData? {
        return try {
            gusService.getCompanyData(nip)
        } catch (e: Exception) {
            // In a real app, log this exception and handle it gracefully
            e.printStackTrace()
            null
        }
    }
}
