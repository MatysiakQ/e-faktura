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
                val rootNavController = rememberNavController()

                NavHost(navController = rootNavController, startDestination = "splash") {
                    composable("splash") { SplashScreen(navController = rootNavController) }
                    composable("main_app") { AppScaffold(rootNavController = rootNavController) } 
                    navigation(startDestination = "login", route = "login_flow") {
                        composable("login") {
                            val backStackEntry = remember(it) { rootNavController.getBackStackEntry("login_flow") }
                            val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = backStackEntry, factory = AppViewModelProvider.Factory)
                            LoginScreen(navController = rootNavController, authViewModel = authViewModel)
                        }
                        composable("register") {
                            val backStackEntry = remember(it) { rootNavController.getBackStackEntry("login_flow") }
                            val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = backStackEntry, factory = AppViewModelProvider.Factory)
                            RegistrationScreen(navController = rootNavController, authViewModel = authViewModel)
                        }
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

    // This effect handles navigation when the user logs out.
    // It remembers the initial state to avoid navigating on first composition.
    val wasUserLoggedIn = remember { mutableStateOf(user != null && user?.isAnonymous == false) }
    LaunchedEffect(user) {
        val isUserLoggedIn = user != null && user?.isAnonymous == false
        // If the user was logged in but isn't anymore, navigate to login.
        if (wasUserLoggedIn.value && !isUserLoggedIn) {
            rootNavController.navigate("login_flow") {
                // Clear the back stack to prevent going back to the authenticated part of the app.
                popUpTo("main_app") { inclusive = true }
            }
        }
        wasUserLoggedIn.value = isUserLoggedIn
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("e-Faktura") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (user == null || user?.isAnonymous == true) {
                            DropdownMenuItem(
                                text = { Text("Zaloguj / Zarejestruj się") },
                                onClick = {
                                    rootNavController.navigate("login_flow")
                                    showMenu = false
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Wyloguj") },
                                onClick = {
                                    authViewModel.logout()
                                    showMenu = false
                                }
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
