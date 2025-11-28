// gotowe
package com.example.e_faktura

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val userId: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult = _authResult.asStateFlow()

    init {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }
    }

    fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser
        return user != null && !user.isAnonymous
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authResult.value = AuthResult.Success(task.result.user!!.uid)
                    } else {
                        _authResult.value = AuthResult.Error(task.exception?.message ?: "Błąd rejestracji")
                    }
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authResult.value = AuthResult.Success(task.result.user!!.uid)
                    } else {
                        _authResult.value = AuthResult.Error(task.exception?.message ?: "Błąd logowania")
                    }
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authResult.value = AuthResult.Success(task.result.user!!.uid)
                    } else {
                        _authResult.value = AuthResult.Error(task.exception?.message ?: "Błąd logowania z Google")
                    }
                }
        }
    }

    fun signOut() {
        auth.signOut()
        auth.signInAnonymously()
        _authResult.value = null
    }

    fun changePassword(password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                user.updatePassword(password).await()
                    .let {
                        onResult(true, "Hasło zostało zmienione.")
                    }
            } else {
                onResult(false, "Użytkownik nie jest zalogowany.")
            }
        }
    }

    fun updateProfilePicture(imageUri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val storageRef = storage.reference.child("profile_pictures/${user.uid}")
                try {
                    val uploadTask = storageRef.putFile(imageUri).await()
                    val downloadUrl = uploadTask.storage.downloadUrl.await()
                    val profileUpdates = userProfileChangeRequest {
                        photoUri = downloadUrl
                    }
                    user.updateProfile(profileUpdates).await()
                    onResult(true, "Zdjęcie profilowe zaktualizowane.")
                } catch (e: Exception) {
                    onResult(false, e.message ?: "Błąd podczas przesyłania zdjęcia.")
                }
            } else {
                onResult(false, "Użytkownik nie jest zalogowany.")
            }
        }
    }
}