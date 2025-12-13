package com.example.e_faktura.data.repository

import android.util.Log
import com.example.e_faktura.data.api.GusService
import com.example.e_faktura.model.Company
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CANONICAL repository for fetching data from the GUS API.
 * This class is the single source of truth for GUS company data.
 */
class GusRepository(private val gusService: GusService) {

    /**
     * Searches for a company by NIP, handling all network logic and mapping internally.
     * @param nip The NIP number of the company.
     * @return A nullable [Company] object. Returns null if the API call fails or data is invalid.
     */
    suspend fun searchByNip(nip: String): Company? {
        return withContext(Dispatchers.IO) {
            try {
                // The GUS API requires the NIP to be in a specific JSON-like format
                val formattedQuery = "pParametryWyszukiwania={\"Nip\":\"$nip\"}"
                val response = gusService.searchByNip(formattedQuery)
                val resultData = response.result

                if (resultData != null) {
                    // Map the DTO to the domain model, providing defaults for missing data
                    Company(
                        nip = resultData.nip ?: nip, // Fallback to the searched NIP
                        businessName = resultData.name ?: "Brak nazwy",
                        address = "${resultData.street ?: ""}, ${resultData.zipCode ?: ""} ${resultData.city ?: ""}".trim(),
                        // These fields are not available from this specific API endpoint
                        ownerFullName = "",
                        bankAccount = ""
                    )
                } else {
                    null // API returned a successful response but with no data
                }
            } catch (e: Exception) {
                Log.e("GusRepository", "GUS API call failed for NIP $nip: ${e.message}")
                null // Return null on any exception (network, parsing, etc.)
            }
        }
    }
}
