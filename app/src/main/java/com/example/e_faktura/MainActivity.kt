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
import com.example.e_faktura.ui.MainScreen
// FIX: Import the correct screen from the 'account' package
import com.example.e_faktura.ui.account.MyAccountScreen
import com.example.e_faktura.ui.auth.LoginScreen
import com.example.e_faktura.ui.company.add.AddCompanyScreen
import com.example.e_faktura.ui.company.details.CompanyDetailsScreen
import com.example.e_faktura.ui.company.edit.EditCompanyScreen
import com.example.e_faktura.ui.invoice.add.AddInvoiceScreen
import com.example.e_faktura.ui.splash.SplashScreen
import com.example.e_faktura.ui.theme.EfakturaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EfakturaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {

                        composable("splash") {
                            SplashScreen(navController = navController)
                        }
                        composable("login") {
                            LoginScreen(navController = navController)
                        }
                        composable("dashboard") {
                            MainScreen(rootNavController = navController)
                        }
                        composable("add_invoice") {
                            AddInvoiceScreen(
                                navController = navController,
                                onInvoiceAdded = { navController.popBackStack() }
                            )
                        }
                        composable("add_company") {
                            AddCompanyScreen(navController = navController)
                        }
                        composable(
                            route = "company_details/{companyId}",
                            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val companyId = backStackEntry.arguments?.getString("companyId")
                            if (companyId != null) {
                                CompanyDetailsScreen(navController = navController, companyId = companyId)
                            }
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

                        // FIX: Ensure this route correctly calls the imported screen
                        composable("my_account"){
                            MyAccountScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
