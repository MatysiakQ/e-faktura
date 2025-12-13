package com.example.e_faktura.data.repository

import com.example.e_faktura.model.Company
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * REPAIRED: Repository is now guest-safe.
 */
class CompanyRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    // This property is now nullable to gracefully handle guests
    private val uid: String?
        get() = firebaseAuth.currentUser?.uid

    fun getCompanies(): Flow<List<Company>> {
        val currentUid = uid
        // If user is not logged in (guest), return a flow with an empty list.
        if (currentUid == null) {
            return flowOf(emptyList())
        }
        return firestore.collection("users").document(currentUid).collection("my_companies")
            .snapshots()
            .map { it.toObjects() }
    }

    suspend fun addCompany(company: Company) {
        val currentUid = uid ?: return // Do nothing if user is a guest
        firestore.collection("users").document(currentUid).collection("my_companies").document(company.id).set(company).await()
    }

    suspend fun updateCompany(company: Company) {
        val currentUid = uid ?: return // Do nothing if user is a guest
        firestore.collection("users").document(currentUid).collection("my_companies").document(company.id).set(company).await()
    }

    suspend fun deleteCompany(companyId: String) {
        val currentUid = uid ?: return // Do nothing if user is a guest
        firestore.collection("users").document(currentUid).collection("my_companies").document(companyId).delete().await()
    }
}
