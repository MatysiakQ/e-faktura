package com.example.e_faktura.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val profileUpdateSuccess: Boolean = false,
    val emailChangeSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val user: StateFlow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser)

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błędny e-mail lub hasło") }
            }
        }
    }

    fun register(email: String, pass: String, username: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                result.user?.updateProfile(profileUpdates)?.await()
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onError(e.localizedMessage ?: "Błąd rejestracji")
            }
        }
    }

    @Suppress("DEPRECATION")
    fun changeEmail(newEmail: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Note: updateEmail jest deprecated, ale verifyBeforeUpdateEmail wymaga interakcji użytkownika
                currentUser.updateEmail(newEmail).await()
                _uiState.update { it.copy(isLoading = false, emailChangeSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun changePassword(newPass: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                currentUser.updatePassword(newPass).await()
                _uiState.update { it.copy(isLoading = false, passwordChangeSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun updateProfilePicture(uri: Uri?) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            if (uri == null) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}")
                val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUrl)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
                _uiState.update { it.copy(isLoading = false, profileUpdateSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState()
    }

    fun resetAuthState() {
        _uiState.update {
            it.copy(
                passwordChangeSuccess = false,
                profileUpdateSuccess = false,
                emailChangeSuccess = false,
                error = null
            )
        }
    }
}
