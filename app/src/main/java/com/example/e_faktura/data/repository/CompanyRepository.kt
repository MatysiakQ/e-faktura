package com.example.e_faktura.data.repository

import com.example.e_faktura.data.local.CompanyDao
import com.example.e_faktura.model.Company
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class CompanyRepository(private val companyDao: CompanyDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllCompaniesStream(): Flow<List<Company>> = companyDao.getAllCompanies()

    suspend fun getCompanyById(id: String): Company? {
        return companyDao.getCompanyById(id)
    }

    suspend fun insertCompany(company: Company) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val companyWithUser = company.copy(userId = userId)
            firestore.collection("companies").document(company.id).set(companyWithUser).await()
            companyDao.insert(companyWithUser)
        } else {
            // Handle the case where the user is not authenticated
            println("Error: User not authenticated, cannot save company.")
        }
    }

    suspend fun refreshCompanies() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                val snapshot = firestore.collection("companies").whereEqualTo("userId", userId).get().await()
                val companies = snapshot.toObjects(Company::class.java)
                companyDao.clearAll() // Clear local cache before refreshing
                companies.forEach { companyDao.insert(it) }
            } catch (e: Exception) {
                // Handle potential Firestore exceptions (e.g., network issues)
                println("Error refreshing companies from Firestore: ${e.message}")
            }
        }
    }
}
