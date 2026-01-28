package com.example.e_faktura.data.repository

import com.example.e_faktura.data.local.CompanyDao
import com.example.e_faktura.model.Company
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class CompanyRepository(
    private val companyDao: CompanyDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    fun getAllCompaniesStream(): Flow<List<Company>> = companyDao.getAllCompanies()

    suspend fun getCompanyById(id: String): Company? = companyDao.getCompanyById(id)

    suspend fun insertCompany(company: Company) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val companyWithUser = company.copy(userId = userId)
            firestore.collection("companies").document(company.id).set(companyWithUser).await()
            companyDao.insert(companyWithUser)
        }
    }

    suspend fun deleteCompany(company: Company) {
        try {
            // Usuwamy z Firestore
            firestore.collection("companies").document(company.id).delete().await()
            // Usuwamy lokalnie z Room
            companyDao.delete(company)
        } catch (e: Exception) {
            println("Error deleting company: ${e.message}")
            throw e
        }
    }

    suspend fun refreshCompanies() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                val snapshot = firestore.collection("companies")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val companies = snapshot.toObjects(Company::class.java)
                companyDao.clearAll()
                companies.forEach { companyDao.insert(it) }
            } catch (e: Exception) {
                println("Error refreshing companies: ${e.message}")
            }
        }
    }
}