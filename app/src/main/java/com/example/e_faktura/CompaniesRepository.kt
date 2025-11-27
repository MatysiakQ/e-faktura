package com.example.e_faktura

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class CompanyRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val localCompanies = MutableStateFlow<List<Company>>(emptyList())

    fun getCompanies(): Flow<List<Company>> {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            callbackFlow {
                val listener = db.collection("users").document(currentUser.uid)
                    .collection("companies")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val companies = snapshot?.documents?.mapNotNull {
                            it.toObject<Company>()
                        } ?: emptyList()
                        trySend(companies)
                    }
                awaitClose { listener.remove() }
            }
        } else {
            localCompanies
        }
    }

    fun addCompany(company: Company) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .collection("companies").document(company.nip)
                .set(company)
        } else {
            localCompanies.value = localCompanies.value + company
        }
    }

    fun deleteCompany(company: Company) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .collection("companies").document(company.nip)
                .delete()
        } else {
            localCompanies.value = localCompanies.value - company
        }
    }

    fun updateCompanyIcon(company: Company, newIcon: CompanyIcon) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .collection("companies").document(company.nip)
                .update("icon", newIcon)
        } else {
            val updatedList = localCompanies.value.map {
                if (it.nip == company.nip) {
                    it.copy(icon = newIcon)
                } else {
                    it
                }
            }
            localCompanies.value = updatedList
        }
    }
}