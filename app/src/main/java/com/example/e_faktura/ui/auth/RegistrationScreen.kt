package com.example.e_faktura.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var login by remember { mutableStateOf<String>("") } // ✅ NOWE POLE
    var email by remember { mutableStateOf<String>("") }
    var password by remember { mutableStateOf<String>("") }
    var confirmPassword by remember { mutableStateOf<String>("") }

    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Utwórz konto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ POLE LOGINU
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Twój Login (widoczny w aplikacji)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (do logowania)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Powtórz hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    onClick = {
                        if (login.isBlank()) {
                            Toast.makeText(context, "Podaj login!", Toast.LENGTH_SHORT).show()
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Hasła się różnią", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 6) {
                            Toast.makeText(context, "Hasło min. 6 znaków", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.register(
                                email = email,
                                pass = password,
                                username = login, // ✅ Przekazujemy login
                                onSuccess = {
                                    Toast.makeText(context, "Konto utworzone!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.MainApp.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                            )
                        }
                    }
                ) { Text("ZAREJESTRUJ SIĘ") }
            }
        }
    }
}