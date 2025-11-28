// gotowe
package com.example.e_faktura

import android.os.Bundle
import android.util.Log
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
import com.example.e_faktura.ui.QrCodeScannerScreen
import com.example.e_faktura.ui.theme.EfakturaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("MainActivity", "Firestore settings already initialized or failed: ${e.message}")
        }

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("MainActivity", "Signed in anonymously successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Anonymous sign-in failed.", e)
                }
        }

        val companyRepository = CompanyRepository()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            val authViewModel: AuthViewModel = viewModel()
            val invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(companyRepository))
            val companyViewModel: CompanyViewModel = viewModel()

            EfakturaTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
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
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }
                        composable("login") {
                            LoginScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("registration") {
                            RegistrationScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("home") {
                            HomeScreen(navController = navController, invoiceViewModel = invoiceViewModel, authViewModel = authViewModel)
                        }
                        composable("my_account") {
                            MyAccountScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("add_company") {
                            AddCompanyScreen(navController = navController, invoiceViewModel = invoiceViewModel, companyViewModel = companyViewModel)
                        }
                        composable("qr_code_scanner") {
                            QrCodeScannerScreen(navController = navController) { qrCodeText ->
                                try {
                                    val qrCodeData = Json.decodeFromString<QrCodeData>(qrCodeText)
                                    val companyType = if (!qrCodeData.ownerFullName.isNullOrBlank()) CompanyType.SOLE_PROPRIETORSHIP else CompanyType.FIRM
                                    companyViewModel.onCompanyTypeChange(companyType)

                                    qrCodeData.businessName?.let { companyViewModel.onBusinessNameChange(it) }
                                    qrCodeData.ownerFullName?.let { companyViewModel.onOwnerFullNameChange(it) }
                                    companyViewModel.onNipChange(qrCodeData.nip)
                                    qrCodeData.address?.let { companyViewModel.onAddressChange(it) }

                                    navController.navigate("add_company") {
                                        popUpTo("qr_code_scanner") { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Nieprawidłowy kod QR lub błąd danych", Toast.LENGTH_SHORT).show()
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