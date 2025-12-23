package com.example.e_faktura.data.dao // ZMIANA PAKIETU Z .local NA .dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Query("SELECT * FROM companies ORDER BY businessName ASC")
    fun getAllCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE id = :id LIMIT 1")
    suspend fun getCompanyById(id: String): Company?

    // ZMIANA: insertCompany zamiast insert (dla spójności z InvoiceDao)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company)

    @Query("DELETE FROM companies")
    suspend fun clearAll()

    // Opcjonalnie usuwanie pojedynczej firmy
    @Query("DELETE FROM companies WHERE id = :id")
    suspend fun deleteCompany(id: String)
}