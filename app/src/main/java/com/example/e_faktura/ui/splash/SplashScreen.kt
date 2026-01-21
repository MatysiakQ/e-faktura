package com.example.e_faktura.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // ✅ Zmieniono z hiltViewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider // ✅ Dodano naszą fabrykę
import com.example.e_faktura.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    // ✅ Używamy ręcznej fabryki zamiast Hilta
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val user by authViewModel.user.collectAsState()
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(800)
        isCheckingAuth = false
    }

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "e-Faktura",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(64.dp))

                AnimatedVisibility(
                    visible = !isCheckingAuth && user == null,
                    enter = fadeIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { navController.navigate("login") },
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            Text("Zaloguj się")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                navController.navigate("dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            Text("Kontynuuj jako gość")
                        }
                    }
                }

                if (isCheckingAuth && user == null) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}