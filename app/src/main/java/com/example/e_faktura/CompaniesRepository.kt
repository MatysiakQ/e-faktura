package com.example.e_faktura

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val localCompanies = MutableStateFlow<List<Company>>(emptyList())

    private val authState: Flow<FirebaseAuth> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.onStart { emit(auth) }

    fun getCompanies(): Flow<List<Company>> {
        return authState.flatMapLatest { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null && !user.isAnonymous) {
                getCompaniesFromFirestore(user.uid)
            } else {
                localCompanies
            }
        }
    }

    private fun getCompaniesFromFirestore(userId: String): Flow<List<Company>> {
        return callbackFlow {
            val listener = db.collection("users").document(userId)
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
    }

    suspend fun addCompany(company: Company) {
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            try {
                db.collection("users").document(currentUser.uid)
                    .collection("companies").document(company.nip)
                    .set(company).await()
            } catch (e: Exception) {
                throw Exception("Błąd zapisu w chmurze: ${e.message}")
            }
        } else {
            localCompanies.value = localCompanies.value + company
        }
    }

    suspend fun deleteCompany(company: Company) {
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            try {
                db.collection("users").document(currentUser.uid)
                    .collection("companies").document(company.nip)
                    .delete().await()
            } catch (e: Exception) {
                throw Exception("Błąd usuwania z chmury: ${e.message}")
            }
        } else {
            localCompanies.value = localCompanies.value - company
        }
    }

    suspend fun updateCompanyIcon(company: Company, newIcon: CompanyIcon) {
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            try {
                db.collection("users").document(currentUser.uid)
                    .collection("companies").document(company.nip)
                    .update("icon", newIcon).await()
            } catch (e: Exception) {
                throw Exception("Błąd aktualizacji w chmurze: ${e.message}")
            }
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