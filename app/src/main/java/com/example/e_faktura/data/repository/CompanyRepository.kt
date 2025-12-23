package com.example.e_faktura.data.repository

// ZMIANA 1: Poprawny import (z .local na .dao)
import com.example.e_faktura.data.dao.CompanyDao
import com.example.e_faktura.model.Company
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor( // ZMIANA 2: Dodano @Inject dla Hilt
    private val companyDao: CompanyDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    fun getAllCompaniesStream(): Flow<List<Company>> = companyDao.getAllCompanies()

    suspend fun getCompanyById(id: String): Company? {
        return companyDao.getCompanyById(id)
    }

    suspend fun insertCompany(company: Company) {
        val userId = firebaseAuth.currentUser?.uid

        // Zapis lokalny (zawsze)
        // ZMIANA 3: companyDao.insert -> companyDao.insertCompany (zgodnie z nowym DAO)
        if (userId != null) {
            val companyWithUser = company.copy(userId = userId)
            // Najpierw Firestore (Online)
            try {
                firestore.collection("companies").document(company.id).set(companyWithUser).await()
            } catch (e: Exception) {
                // Ignorujemy błąd sieci, zapisujemy lokalnie
                e.printStackTrace()
            }
            // Potem Room (Offline/Cache)
            companyDao.insertCompany(companyWithUser)
        } else {
            // Tryb gościa - tylko lokalnie
            companyDao.insertCompany(company)
        }
    }

    suspend fun refreshCompanies() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                val snapshot = firestore.collection("companies").whereEqualTo("userId", userId).get().await()
                val companies = snapshot.toObjects(Company::class.java)

                // Aktualizuj lokalną bazę danymi z chmury
                companyDao.clearAll()
                // ZMIANA 4: insert -> insertCompany
                companies.forEach { companyDao.insertCompany(it) }
            } catch (e: Exception) {
                println("Error refreshing companies from Firestore: ${e.message}")
            }
        }
    }
}