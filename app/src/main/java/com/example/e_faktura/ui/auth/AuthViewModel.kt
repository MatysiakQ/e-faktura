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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val profileUpdateSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Flow obserwujący aktualnego użytkownika
    val user: StateFlow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser)

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // await() sprawia, że czekamy na wynik, jeśli się nie uda, rzuci wyjątek
                auth.signInWithEmailAndPassword(email, pass).await()
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Błąd logowania") }
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

    fun changePassword(newPass: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(error = "Użytkownik nie jest zalogowany") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                currentUser.updatePassword(newPass).await()
                _uiState.update { it.copy(isLoading = false, passwordChangeSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Błąd zmiany hasła") }
            }
        }
    }

    fun updateProfilePicture(uri: Uri?) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null || uri == null) return@launch

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}")
                val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()

                val profileUpdates = userProfileChangeRequest { photoUri = downloadUrl }
                currentUser.updateProfile(profileUpdates).await()

                _uiState.update { it.copy(isLoading = false, profileUpdateSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Błąd wysyłania zdjęcia") }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    // --- ZMIANA NAZWY Z resetState NA resetAuthState ---
    fun resetAuthState() {
        _uiState.update { AuthUiState() }
    }
}