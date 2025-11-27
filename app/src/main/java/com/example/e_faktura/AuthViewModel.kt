package com.example.e_faktura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthResult {
    data class Success(val userId: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult = _authResult.asStateFlow()

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

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

    fun signOut() {
        auth.signOut()
        _authResult.value = null // Reset state after sign out
    }
}