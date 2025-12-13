package com.example.e_faktura.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    /**
     * A reactive flow providing the current Firebase user.
     * The UI should observe this to react to login/logout changes.
     */
    val user: StateFlow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser)

    /**
     * DEPRECATED: Synchronously gets the current user. Prefer observing the `user` flow.
     * Provided for compatibility with existing UI code.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Błąd logowania")
            }
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Błąd rejestracji")
            }
        }
    }

    fun updateProfilePicture(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onError("Użytkownik nie jest zalogowany.")
                return@launch
            }
            try {
                val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}")
                val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()
                val profileUpdates = userProfileChangeRequest { photoUri = downloadUrl }
                currentUser.updateProfile(profileUpdates).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Błąd podczas przesyłania zdjęcia.")
            }
        }
    }

    fun changePassword(newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onError("Użytkownik nie jest zalogowany.")
                return@launch
            }
            try {
                currentUser.updatePassword(newPass).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Wystąpił błąd podczas zmiany hasła.")
            }
        }
    }

    fun logout() {
        auth.signOut()
        // For guest mode, immediately create a new anonymous session
        viewModelScope.launch {
            auth.signInAnonymously()
        }
    }
}