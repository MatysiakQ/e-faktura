package com.example.e_faktura

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.e_faktura.ui.theme.EfakturaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            val authViewModel: AuthViewModel = viewModel()

            EfakturaTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val invoiceViewModel: InvoiceViewModel = viewModel()
                    val companyViewModel: CompanyViewModel = viewModel()
                    val context = LocalContext.current

                    LaunchedEffect(authViewModel.authResult) {
                        authViewModel.authResult.collect {
                            when (it) {
                                is AuthResult.Success -> {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                                is AuthResult.Error -> {
                                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                }
                                else -> Unit
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("registration") {
                            RegistrationScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("home") {
                            HomeScreen(navController = navController, invoiceViewModel = invoiceViewModel)
                        }
                        composable("add_company") {
                            AddCompanyScreen(navController = navController, invoiceViewModel = invoiceViewModel, companyViewModel = companyViewModel)
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                isDarkTheme = isDarkTheme,
                                onThemeChange = { settingsViewModel.setDarkTheme(it) }
                            )
                        }
                        composable(
                            "company_details/{nip}",
                            arguments = listOf(navArgument("nip") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val nip = backStackEntry.arguments?.getString("nip")
                            val companies by invoiceViewModel.companies.collectAsState()
                            val company = companies.find { it.nip == nip }
                            if (company != null) {
                                CompanyDetailsScreen(
                                    navController = navController,
                                    company = company,
                                    invoiceViewModel = invoiceViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}