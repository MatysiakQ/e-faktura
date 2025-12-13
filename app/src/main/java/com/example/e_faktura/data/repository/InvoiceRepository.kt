package com.example.e_faktura.data.repository

import com.example.e_faktura.model.Invoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class InvoiceRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val uid: String?
        get() = firebaseAuth.currentUser?.uid

    fun getInvoices(): Flow<List<Invoice>> {
        val currentUid = uid
        if (currentUid == null) {
            return flowOf(emptyList())
        }
        return firestore.collection("users").document(currentUid).collection("invoices")
            .snapshots()
            .map { it.toObjects() }
    }

    suspend fun addInvoice(invoice: Invoice) {
        val currentUid = uid ?: return
        firestore.collection("users").document(currentUid).collection("invoices").document(invoice.id).set(invoice).await()
    }

    suspend fun updateInvoice(invoice: Invoice) {
        val currentUid = uid ?: return
        firestore.collection("users").document(currentUid).collection("invoices").document(invoice.id).set(invoice).await()
    }

    suspend fun deleteInvoice(invoiceId: String) {
        val currentUid = uid ?: return
        // CORRECTED: Removed the invalid newline character from the string literal
        firestore.collection("users").document(currentUid).collection("invoices").document(invoiceId).delete().await()
    }
}
