package com.example.e_faktura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint

// Importy ekranów
import com.example.e_faktura.ui.auth.LoginScreen
// import com.example.e_faktura.ui.auth.RegisterScreen
// import com.example.e_faktura.ui.account.MyAccountScreen

import com.example.e_faktura.ui.company.add.AddCompanyScreen
import com.example.e_faktura.ui.company.edit.EditCompanyScreen
import com.example.e_faktura.ui.company.list.CompanyListScreen
import com.example.e_faktura.ui.company.details.CompanyDetailsScreen
import com.example.e_faktura.ui.invoice.add.AddInvoiceScreen
import com.example.e_faktura.ui.invoice.list.InvoiceDashboardScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {

                        // --- AUTENTYKACJA ---
                        composable("login") {
                            LoginScreen(navController = navController)
                        }

                        /* composable("register") {
                            RegisterScreen(navController = navController)
                        }
                        */

                        // --- FAKTURY ---
                        composable("dashboard") {
                            InvoiceDashboardScreen(navController = navController)
                        }

                        composable("add_invoice") {
                            // POPRAWKA: Przekazujemy wymagany callback
                            AddInvoiceScreen(
                                navController = navController,
                                onInvoiceAdded = {
                                    // Po pomyślnym dodaniu faktury wracamy do poprzedniego ekranu
                                    navController.popBackStack()
                                }
                            )
                        }

                        // --- FIRMY ---
                        composable("company_list") {
                            CompanyListScreen(navController = navController)
                        }

                        composable("add_company") {
                            AddCompanyScreen(navController = navController)
                        }

                        composable(
                            route = "edit_company/{companyId}",
                            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val companyId = backStackEntry.arguments?.getString("companyId")
                            if (companyId != null) {
                                EditCompanyScreen(navController = navController, companyId = companyId)
                            }
                        }

                        composable(
                            route = "company_details/{companyId}",
                            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val companyId = backStackEntry.arguments?.getString("companyId")
                            if (companyId != null) {
                                // POPRAWKA: Przekazujemy wymagane ID do ekranu
                                CompanyDetailsScreen(
                                    navController = navController,
                                    companyId = companyId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}