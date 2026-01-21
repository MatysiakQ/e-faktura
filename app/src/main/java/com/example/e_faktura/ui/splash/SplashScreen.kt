package com.example.e_faktura.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
// Ten poniższy import jest KLUCZOWY, żeby "by" nie świeciło się na czerwono:
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // POPRAWKA: Usunięto (initial = null).
    // StateFlow zawsze ma wartość, więc collectAsState() wystarczy.
    val user by authViewModel.user.collectAsState()

    // Lokalny stan, aby dać aplikacji chwilę na sprawdzenie Firebase (zapobiega mignięciu przycisków)
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Małe opóźnienie (np. 800ms), żeby logo się wyświetliło, a Firebase zdążył odpowiedzieć
        delay(800)
        isCheckingAuth = false
    }

    LaunchedEffect(user) {
        // Jeśli user nie jest nullem = jest zalogowany -> idziemy do Dashboard
        if (user != null) {
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LOGO
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

                // Wyświetlamy przyciski TYLKO, gdy minie czas sprawdzania I użytkownik nadal jest null
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

                // Spinner wyświetla się tylko podczas sprawdzania
                if (isCheckingAuth && user == null) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}