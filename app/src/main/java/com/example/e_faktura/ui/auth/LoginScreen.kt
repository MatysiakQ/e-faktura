package com.example.e_faktura.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var email by remember { mutableStateOf<String>("") }
    var password by remember { mutableStateOf<String>("") }
    val state by viewModel.uiState.collectAsState()

    // Reakcja na sukces logowania
    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) {
            navController.navigate(Screen.MainApp.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Witaj w e-Faktura", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )

        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.login(email, password) }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("Zaloguj się")
            }
        }

        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("Nie masz konta? Zarejestruj się")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        OutlinedButton(
            onClick = {
                navController.navigate(Screen.MainApp.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("KONTYNUUJ JAKO GOŚĆ")
        }
    }
}