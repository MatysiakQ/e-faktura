package com.example.e_faktura.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): Invoice?

    @Query("SELECT * FROM invoices ORDER BY dueDate DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY dueDate DESC")
    fun getInvoicesByUser(userId: String): Flow<List<Invoice>>

    @Query("DELETE FROM invoices")
    suspend fun deleteAllInvoices()
}