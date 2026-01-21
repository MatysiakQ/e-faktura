package com.example.e_faktura.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // ✅ ZMIENIONO
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider // ✅ DODANO
import com.example.e_faktura.ui.navigation.Screen

@Composable
fun RegistrationScreen(
    navController: NavController,
    // ✅ UŻYWAMY RĘCZNEJ FABRYKI
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Hasło") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Potwierdź hasło") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(context, "Hasła nie są zgodne", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.register(email, password,
                    onSuccess = {
                        navController.navigate(Screen.MainApp.route) { popUpTo(Screen.Register.route) { inclusive = true } }
                    },
                    onError = { error -> Toast.makeText(context, error, Toast.LENGTH_LONG).show() }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zarejestruj się")
        }
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Masz już konto? Zaloguj się")
        }
    }
}