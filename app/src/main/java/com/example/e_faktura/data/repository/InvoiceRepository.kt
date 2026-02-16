package com.example.e_faktura.data.repository

import com.example.e_faktura.data.local.InvoiceDao
import com.example.e_faktura.model.Invoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val userId: String?
        get() = firebaseAuth.currentUser?.uid

    suspend fun getInvoiceById(id: String): Invoice? {
        val currentUserId = userId
        return if (currentUserId == null) {
            invoiceDao.getInvoiceById(id)
        } else {
            try {
                val snapshot = firestore.collection("users").document(currentUserId)
                    .collection("invoices").document(id).get().await()

                // Jeśli nie ma w Firebase, szukaj lokalnie
                snapshot.toObject(Invoice::class.java) ?: invoiceDao.getInvoiceById(id)
            } catch (e: Exception) {
                invoiceDao.getInvoiceById(id)
            }
        }
    }

    fun getInvoices(): Flow<List<Invoice>> {
        val currentUserId = userId
        return if (currentUserId == null) {
            invoiceDao.getAllInvoices()
        } else {
            callbackFlow {
                val listener = firestore.collection("users").document(currentUserId)
                    .collection("invoices")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            close(e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val invoices = snapshot.documents.mapNotNull { it.toObject(Invoice::class.java) }
                            trySend(invoices)
                        }
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    suspend fun addInvoice(invoice: Invoice) {
        val currentUserId = userId
        val currentTime = System.currentTimeMillis()

        // ✅ OBLICZENIE: 14 dni w milisekundach (14 * 24 * 60 * 60 * 1000)
        val twoWeeksInMs = 1_209_600_000L
        val automaticDueDate = currentTime + twoWeeksInMs

        val finalInvoice = invoice.copy(
            userId = currentUserId ?: "",
            // Użyj dueDate z faktury jeśli ustawiony, inaczej automatyczny (2 tygodnie)
            dueDate = if (invoice.dueDate > 0) invoice.dueDate else automaticDueDate,
            isPaid = false
        )

        invoiceDao.insertInvoice(finalInvoice)

        if (currentUserId != null) {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(finalInvoice.id).set(finalInvoice).await()
        }
    }

    suspend fun updateInvoice(invoice: Invoice) {
        val currentUserId = userId
        // ✅ NAPRAWIONO: 'updateInvoice' zamiast 'update'
        invoiceDao.updateInvoice(invoice)

        if (currentUserId != null) {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(invoice.id).set(invoice).await()
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        val currentUserId = userId
        // ✅ NAPRAWIONO: 'deleteInvoice' zamiast 'delete'
        invoiceDao.deleteInvoice(invoice)

        if (currentUserId != null) {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(invoice.id).delete().await()
        }
    }
}