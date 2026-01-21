package com.example.e_faktura.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.e_faktura.model.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    // Zmiana: ORDER BY name zamiast name
    @Query("SELECT * from companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<Company>>

    @Query("SELECT * from companies WHERE id = :id")
    suspend fun getCompanyById(id: String): Company?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company)

    @Update
    suspend fun update(company: Company)

    @Delete
    suspend fun delete(company: Company)

    @Query("DELETE FROM companies")
    suspend fun clearAll()
}