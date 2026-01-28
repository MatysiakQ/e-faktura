package com.example.e_faktura

// ✅ DODANO: Importy modelu i enuma
import com.example.e_faktura.model.Company
import com.example.e_faktura.model.CompanyType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the Company data class.
 */
class CompanyTest {

    @Test
    fun `displayName for FIRM returns name when not null`() {
        val company = Company(
            type = CompanyType.FIRM,
            name = "Test Corp",
            ownerFullName = "John Doe"
        )
        assertEquals("Test Corp", company.displayName)
    }

    @Test
    fun `displayName for FIRM returns fallback when name is null`() {
        val company = Company(
            type = CompanyType.FIRM,
            name = null
        )
        assertEquals("Brak nazwy", company.displayName)
    }

    @Test
    fun `displayName for SOLE_PROPRIETORSHIP returns ownerFullName when not null`() {
        val company = Company(
            type = CompanyType.SOLE_PROPRIETORSHIP,
            name = "Test Corp",
            ownerFullName = "Jane Doe"
        )
        assertEquals("Jane Doe", company.displayName)
    }

    @Test
    fun `displayName for SOLE_PROPRIETORSHIP returns fallback when ownerFullName is null`() {
        val company = Company(
            type = CompanyType.SOLE_PROPRIETORSHIP,
            ownerFullName = null
        )
        assertEquals("Brak nazwy", company.displayName)
    }
}