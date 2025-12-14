package com.example.e_faktura

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.QrCodeScannerScreen
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.auth.LoginScreen
import com.example.e_faktura.ui.auth.RegistrationScreen
import com.example.e_faktura.ui.company.add.AddCompanyScreen
import com.example.e_faktura.ui.company.details.CompanyDetailsScreen
import com.example.e_faktura.ui.company.list.CompanyListScreen
import com.example.e_faktura.ui.core.SplashScreen
import com.example.e_faktura.ui.dashboard.StatisticsScreen
import com.example.e_faktura.ui.invoice.add.AddInvoiceScreen
import com.example.e_faktura.ui.invoice.list.InvoiceDashboardScreen
import com.example.e_faktura.ui.navigation.Screen
import com.example.e_faktura.ui.navigation.bottomNavItems
import com.example.e_faktura.ui.theme.EfakturaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EfakturaTheme {
                val rootNavController = rememberNavController()

                NavHost(navController = rootNavController, startDestination = Screen.Splash.route) {
                    composable(Screen.Splash.route) { SplashScreen(navController = rootNavController) }
                    composable(Screen.MainApp.route) { AppScaffold(rootNavController = rootNavController) }
                    composable(Screen.Login.route) {
                        val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
                        LoginScreen(navController = rootNavController, authViewModel = authViewModel)
                    }
                    composable(Screen.Register.route) {
                        val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
                        RegistrationScreen(navController = rootNavController, authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val user by authViewModel.user.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val rootRoutes = remember { bottomNavItems.map { it.route } }

    val isFullScreenRoute = currentRoute == Screen.AddCompany.route 
        || currentRoute == Screen.AddInvoice.route 
        || currentRoute?.startsWith("company_details/") == true

    val showGlobalBars = currentRoute in rootRoutes && !isFullScreenRoute

    val isLoggedIn = user != null && user?.isAnonymous == false

    Scaffold(
        topBar = {
            if (showGlobalBars) {
                TopAppBar(
                    title = { Text(if (isLoggedIn) user?.email ?: "e-Faktura" else "Witaj, Gościu") },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (isLoggedIn) {
                                DropdownMenuItem(
                                    text = { Text("Wyloguj") },
                                    onClick = {
                                        authViewModel.logout()
                                        showMenu = false
                                        rootNavController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.MainApp.route) { inclusive = true }
                                        }
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Zaloguj") },
                                    onClick = {
                                        rootNavController.navigate(Screen.Login.route)
                                        showMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(text = { Text("Ustawienia") }, onClick = { /* TODO */ ; showMenu = false })
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showGlobalBars) {
                FloatingActionButton(onClick = {
                    when (currentRoute) {
                        Screen.Home.route -> navController.navigate(Screen.AddInvoice.route)
                        Screen.Companies.route -> navController.navigate(Screen.AddCompany.route)
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj")
                }
            }
        },
        bottomBar = {
            if (showGlobalBars) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        screen.icon?.let { icon ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = screen.label) },
                                label = { screen.label?.let { Text(it) } },
                                selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
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
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { InvoiceDashboardScreen(navController = navController) }
            composable(Screen.Companies.route) { CompanyListScreen(navController = navController) }
            composable(Screen.Statistics.route) { StatisticsScreen(navController) }
            composable(Screen.AddCompany.route) { AddCompanyScreen(onCompanyAdded = { navController.popBackStack() }) }
            composable(Screen.AddInvoice.route) { AddInvoiceScreen(navController = navController, onInvoiceAdded = { navController.popBackStack() }) }
            composable(Screen.QrScanner.route) {
                QrCodeScannerScreen(navController = navController, onQrCodeScanned = { scannedNip ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scannedNip", scannedNip)
                    navController.popBackStack()
                })
            }
            composable(
                route = "company_details/{companyId}",
                arguments = listOf(navArgument("companyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getString("companyId")
                Log.d("AppNavigation", "Navigating to details with ID: $companyId")
                CompanyDetailsScreen(navController = navController)
            }
        }
    }
}