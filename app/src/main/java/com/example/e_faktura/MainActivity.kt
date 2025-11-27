package com.example.e_faktura

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.example.e_faktura.ui.QrCodeScannerScreen
import com.example.e_faktura.ui.theme.EfakturaTheme
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            EfakturaTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val invoiceViewModel: InvoiceViewModel = viewModel()
                    val companyViewModel: CompanyViewModel = viewModel()
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }
                        composable("home") {
                            HomeScreen(navController = navController, invoiceViewModel = invoiceViewModel)
                        }
                        composable("add_company") {
                            AddCompanyScreen(navController = navController, invoiceViewModel = invoiceViewModel, companyViewModel = companyViewModel)
                        }
                        composable("qr_code_scanner") { // New route
                            val context = LocalContext.current
                            QrCodeScannerScreen(navController = navController) { qrCodeText ->
                                try {
                                    val qrCodeData = Json.decodeFromString<QrCodeData>(qrCodeText)
                                    // Determine company type
                                    val companyType = if (!qrCodeData.ownerFullName.isNullOrBlank()) {
                                        CompanyType.SOLE_PROPRIETORSHIP
                                    } else {
                                        CompanyType.FIRM
                                    }
                                    companyViewModel.onCompanyTypeChange(companyType)

                                    // Update view model
                                    qrCodeData.businessName?.let { companyViewModel.onBusinessNameChange(it) }
                                    qrCodeData.ownerFullName?.let { companyViewModel.onOwnerFullNameChange(it) }
                                    companyViewModel.onNipChange(qrCodeData.nip)
                                    qrCodeData.address?.let { companyViewModel.onAddressChange(it) }

                                    // Navigate to add company screen
                                    navController.navigate("add_company") {
                                        // Pop the scanner screen from the back stack
                                        popUpTo("qr_code_scanner") { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    // Handle JSON parsing error, maybe show a toast
                                    e.printStackTrace()
                                    Toast.makeText(context, "Nieprawidłowy kod QR lub błąd danych", Toast.LENGTH_SHORT).show()
                                    // Go back if scanning fails or is cancelled
                                    navController.popBackStack()
                                }
                            }
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