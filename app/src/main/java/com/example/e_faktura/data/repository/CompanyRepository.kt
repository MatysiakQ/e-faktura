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

    // BUG #11 FIX: zachowaj userId z bazy jeśli EditCompanyViewModel go nie przekazał
    suspend fun updateCompany(company: Company) {
        val userId = firebaseAuth.currentUser?.uid

        // Pobierz istniejącą firmę żeby zachować userId i inne metadane
        val existing = companyDao.getCompanyById(company.id)
        val companyWithUser = company.copy(
            userId = userId ?: existing?.userId ?: company.userId
        )

        companyDao.update(companyWithUser)

        if (userId != null) {
            try {
                firestore.collection("companies")
                    .document(companyWithUser.id)
                    .set(companyWithUser)
                    .await()
            } catch (e: Exception) {
                // Lokalnie zapisano — Firebase sync po przywróceniu połączenia
            }
        }
    }

    suspend fun deleteCompany(company: Company) {
        try {
            firestore.collection("companies").document(company.id).delete().await()
            companyDao.delete(company)
        } catch (e: Exception) {
            // Jeśli Firebase nie odpowiada — usuń lokalnie, sync przy reconnect
            companyDao.delete(company)
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
                // Zostają dane lokalne
            }
        }
    }
}
