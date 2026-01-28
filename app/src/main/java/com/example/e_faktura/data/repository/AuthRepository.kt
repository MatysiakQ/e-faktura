package com.example.e_faktura.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore) {

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    suspend fun login(email: String, pass: String): FirebaseUser? {
        return firebaseAuth.signInWithEmailAndPassword(email, pass).await().user
    }

    suspend fun register(email: String, pass: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        result.user?.let {
            firestore.collection("users").document(it.uid).set(mapOf("email" to email)).await()
        }
        return result.user
    }

    fun logout() {
        firebaseAuth.signOut()
    }


    suspend fun findUserByEmail(email: String): String? {
        return try {
            val query = firestore.collection("users").whereEqualTo("email", email).limit(1).get().await()
            if (query.isEmpty) {
                null
            } else {
                query.documents.first().id
            }
        } catch (e: Exception) {
            null
        }
    }
}
