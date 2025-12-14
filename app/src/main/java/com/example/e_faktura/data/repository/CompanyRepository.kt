package com.example.e_faktura.data.repository

import com.example.e_faktura.model.Company
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CompanyRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val uid: String?
        get() = firebaseAuth.currentUser?.uid

    fun getCompanies(): Flow<List<Company>> {
        val currentUid = uid ?: return flowOf(emptyList())
        return firestore.collection("users").document(currentUid).collection("my_companies")
            .snapshots()
            .map { it.toObjects() }
    }

    fun getCompanyById(companyId: String): Flow<Company?> {
        val currentUid = uid ?: return flowOf(null)
        return firestore.collection("users").document(currentUid).collection("my_companies").document(companyId)
            .snapshots()
            .map { it.toObject<Company>() }
    }

    suspend fun addCompany(company: Company) {
        val currentUid = uid ?: return
        firestore.collection("users").document(currentUid).collection("my_companies").document(company.id).set(company).await()
    }

    suspend fun updateCompany(company: Company) {
        val currentUid = uid ?: return
        firestore.collection("users").document(currentUid).collection("my_companies").document(company.id).set(company).await()
    }

    suspend fun deleteCompany(companyId: String) {
        val currentUid = uid ?: return
        firestore.collection("users").document(currentUid).collection("my_companies").document(companyId).delete().await()
    }
}
