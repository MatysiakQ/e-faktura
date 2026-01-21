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
        // ✅ NAPRAWIONO: 'userId' zamiast 'sellerId'
        val invoiceWithUser = if (currentUserId != null) invoice.copy(userId = currentUserId) else invoice

        // ✅ NAPRAWIONO: 'insertInvoice' zamiast 'insert'
        invoiceDao.insertInvoice(invoiceWithUser)

        if (currentUserId != null) {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(invoice.id).set(invoiceWithUser).await()
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