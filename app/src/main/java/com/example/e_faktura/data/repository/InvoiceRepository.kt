package com.example.e_faktura.data.repository

import com.example.e_faktura.data.dao.InvoiceDao // Sprawdź czy pakiet to .dao czy .local u Ciebie
import com.example.e_faktura.model.Invoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val userId: String?
        get() = firebaseAuth.currentUser?.uid

    fun getInvoices(): Flow<List<Invoice>> {
        val currentUserId = userId
        if (currentUserId == null) {
            // Tryb gościa (Offline - Room)
            return invoiceDao.getAllInvoices()
        } else {
            // Zalogowany (Online - Firestore)
            return callbackFlow {
                val listener = firestore.collection("users").document(currentUserId)
                    .collection("invoices")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            close(e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            trySend(snapshot.toObjects<Invoice>())
                        }
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    // Dodana funkcja, której może brakować w innych miejscach
    suspend fun getInvoiceById(id: String): Invoice? {
        val currentUserId = userId
        if (currentUserId == null) {
            return invoiceDao.getInvoice(id)
        } else {
            val snapshot = firestore.collection("users").document(currentUserId)
                .collection("invoices").document(id).get().await()
            return snapshot.toObject(Invoice::class.java)
        }
    }

    // ZMIANA NAZWY: addInvoice -> insertInvoice (zgodnie z ViewModel)
    suspend fun insertInvoice(invoice: Invoice) {
        val currentUserId = userId
        if (currentUserId == null) {
            invoiceDao.insertInvoice(invoice)
        } else {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(invoice.id).set(invoice).await()
        }
    }

    suspend fun updateInvoice(invoice: Invoice) {
        val currentUserId = userId
        if (currentUserId == null) {
            // Room zazwyczaj używa tego samego insert z onConflict=REPLACE,
            // ale jeśli masz update w DAO, to ok.
            invoiceDao.insertInvoice(invoice)
        } else {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(invoice.id).set(invoice).await()
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        val currentUserId = userId
        if (currentUserId == null) {
            invoiceDao.deleteInvoice(invoice.id) // DAO zazwyczaj usuwa po ID
        } else {
            firestore.collection("users").document(currentUserId)
                .collection("invoices").document(invoice.id).delete().await()
        }
    }
}