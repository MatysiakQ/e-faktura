package com.example.e_faktura.data.local

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company)

    @Query("DELETE FROM companies")
    suspend fun clearAll()
}
