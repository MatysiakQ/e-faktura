package com.example.e_faktura

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the Company data class.
 * This test class verifies the correctness of the business logic within the Company model,
 * specifically focusing on the `displayName` computed property.
 */
class CompanyTest {

    @Test
    fun `displayName for FIRM returns businessName when not null`() {
        // Arrange
        val company = Company(
            type = CompanyType.FIRM,
            businessName = "Test Corp",
            ownerFullName = "John Doe" // Should be ignored
        )

        // Act
        val displayName = company.displayName

        // Assert
        assertEquals("Test Corp", displayName)
    }

    @Test
    fun `displayName for FIRM returns fallback when businessName is null`() {
        // Arrange
        val company = Company(
            type = CompanyType.FIRM,
            businessName = null
        )

        // Act
        val displayName = company.displayName

        // Assert
        assertEquals("Brak nazwy", displayName)
    }

    @Test
    fun `displayName for SOLE_PROPRIETORSHIP returns ownerFullName when not null`() {
        // Arrange
        val company = Company(
            type = CompanyType.SOLE_PROPRIETORSHIP,
            businessName = "Test Corp", // Should be ignored
            ownerFullName = "Jane Doe"
        )

        // Act
        val displayName = company.displayName

        // Assert
        assertEquals("Jane Doe", displayName)
    }

    @Test
    fun `displayName for SOLE_PROPRIETORSHIP returns fallback when ownerFullName is null`() {
        // Arrange
        val company = Company(
            type = CompanyType.SOLE_PROPRIETORSHIP,
            ownerFullName = null
        )

        // Act
        val displayName = company.displayName

        // Assert
        assertEquals("Brak nazwy", displayName)
    }
}
