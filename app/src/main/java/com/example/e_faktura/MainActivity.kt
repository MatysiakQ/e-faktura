package com.example.e_faktura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.QrCodeScannerScreen
import com.example.e_faktura.ui.core.SplashScreen
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.auth.LoginScreen
import com.example.e_faktura.ui.auth.RegistrationScreen
import com.example.e_faktura.ui.company.add.AddCompanyScreen
import com.example.e_faktura.ui.company.list.CompanyListScreen
import com.example.e_faktura.ui.invoice.add.AddInvoiceScreen
import com.example.e_faktura.ui.invoice.list.InvoiceDashboardScreen
import com.example.e_faktura.ui.theme.EfakturaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EfakturaTheme {
                // The single, root NavController for the entire application
                val rootNavController = rememberNavController()

                NavHost(navController = rootNavController, startDestination = "splash") {
                    composable("splash") { SplashScreen(navController = rootNavController) }
                    // The main app is now a single destination with its own internal navigation
                    composable("main_app") { AppScaffold(rootNavController = rootNavController) } 
                    // The login flow is a separate, nested graph
                    navigation(startDestination = "login", route = "login_flow") {
                        composable("login") { LoginScreen(navController = rootNavController, authViewModel = viewModel(factory = AppViewModelProvider.Factory)) }
                        composable("register") { RegistrationScreen(navController = rootNavController, authViewModel = viewModel(factory = AppViewModelProvider.Factory)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(rootNavController: NavHostController) {
    // DEFINITIVE FIX: This is the ONLY NavController for the main app UI.
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val user by authViewModel.user.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("e-Faktura") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (user == null) {
                            DropdownMenuItem(
                                text = { Text("Zaloguj / Zarejestruj się") },
                                // Use the ROOT controller to navigate outside the scaffold
                                onClick = { rootNavController.navigate("login_flow"); showMenu = false }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Wyloguj") },
                                onClick = { authViewModel.logout(); showMenu = false }
                            )
                        }
                        DropdownMenuItem(text = { Text("Ustawienia") }, onClick = { /* TODO */ ; showMenu = false })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == Screen.Home.route) {
                    navController.navigate("add_invoice")
                } else if (currentRoute == Screen.Companies.route) {
                    navController.navigate("add_company")
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        },
        bottomBar = {
            // The BottomBar now correctly uses the local navController
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // The NavHost now correctly uses the same controller as the BottomBar and FAB
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { InvoiceDashboardScreen(navController = navController) }
            composable(Screen.Companies.route) { CompanyListScreen() }
            composable("add_company") { AddCompanyScreen(onCompanyAdded = { navController.popBackStack() }) }
            composable("add_invoice") { AddInvoiceScreen(navController = navController, onInvoiceAdded = { navController.popBackStack() }) }
            composable("scan_qr") {
                QrCodeScannerScreen(navController = navController, onQrCodeScanned = { scannedNip ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scannedNip", scannedNip)
                    navController.popBackStack()
                })
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Pulpit", Icons.Filled.Dashboard)
    object Companies : Screen("companies", "Moje Firmy", Icons.Filled.Business)
}

val bottomNavItems = listOf(Screen.Home, Screen.Companies)
