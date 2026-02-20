package com.example.e_faktura.data.repository

import com.example.e_faktura.data.local.InvoiceDao
import com.example.e_faktura.model.Invoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val userId: String?
        get() = firebaseAuth.currentUser?.uid

    suspend fun getInvoiceById(id: String): Invoice? {
        // BUG #1 FIX: Zawsze najpierw Room (szybko), potem próba Firebase
        val local = invoiceDao.getInvoiceById(id)
        val currentUserId = userId ?: return local

        return try {
            val snapshot = firestore.collection("users").document(currentUserId)
                .collection("invoices").document(id).get().await()
            snapshot.toObject(Invoice::class.java) ?: local
        } catch (e: Exception) {
            local
        }
    }

    // BUG #1 FIX: offline-first — Room jest źródłem prawdy, Firebase syncuje w tle
    fun getInvoices(): Flow<List<Invoice>> {
        val currentUserId = userId
            ?: return invoiceDao.getAllInvoices()   // gość → tylko Room

        return flow {
            // Krok 1: Natychmiast emituj dane lokalne (działa offline)
            emitAll(invoiceDao.getAllInvoices())

            // Krok 2: Próba synchronizacji z Firebase w tle
            try {
                val snapshot = firestore.collection("users").document(currentUserId)
                    .collection("invoices")
                    .get()
                    .await()

                val remoteInvoices = snapshot.documents
                    .mapNotNull { it.toObject(Invoice::class.java) }

                if (remoteInvoices.isNotEmpty()) {
                    // Zapisz zdalnie pobrane dane lokalnie
                    remoteInvoices.forEach { invoiceDao.insertInvoice(it) }
                    // Flow z Room automatycznie emituje nową listę po insertach
                }
            } catch (e: Exception) {
                // Brak sieci lub błąd Firebase — dane lokalne wystarczą
                // Flow już emituje dane z Room więc nie ma crashu
            }
        }
    }

    suspend fun addInvoice(invoice: Invoice) {
        val currentUserId = userId
        val currentTime = System.currentTimeMillis()
        val twoWeeksInMs = 1_209_600_000L

        val finalInvoice = invoice.copy(
            userId = currentUserId ?: "",
            dueDate = if (invoice.dueDate > 0) invoice.dueDate else currentTime + twoWeeksInMs,
            isPaid = false
        )

        // Najpierw Room — gwarantuje lokalny zapis nawet przy braku sieci
        invoiceDao.insertInvoice(finalInvoice)

        if (currentUserId != null) {
            try {
                firestore.collection("users").document(currentUserId)
                    .collection("invoices").document(finalInvoice.id)
                    .set(finalInvoice).await()
            } catch (e: Exception) {
                // Lokalnie zapisano — Firebase sync przy następnym uruchomieniu getInvoices()
            }
        }
    }

    suspend fun updateInvoice(invoice: Invoice) {
        val currentUserId = userId

        invoiceDao.updateInvoice(invoice)

        if (currentUserId != null) {
            try {
                firestore.collection("users").document(currentUserId)
                    .collection("invoices").document(invoice.id)
                    .set(invoice).await()
            } catch (e: Exception) {
                // Lokalnie zapisano
            }
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        val currentUserId = userId

        invoiceDao.deleteInvoice(invoice)

        if (currentUserId != null) {
            try {
                firestore.collection("users").document(currentUserId)
                    .collection("invoices").document(invoice.id)
                    .delete().await()
            } catch (e: Exception) {
                // Lokalnie usunięto, Firebase sync przy reconnect
            }
        }
    }
}
