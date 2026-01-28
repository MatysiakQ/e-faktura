package com.example.e_faktura

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.e_faktura.data.network.NetworkChangeReceiver
import com.example.e_faktura.data.sync.SyncService
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.QrCodeScannerScreen
import com.example.e_faktura.ui.account.MyAccountScreen
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
import com.example.e_faktura.ui.invoice.details.InvoiceDetailsScreen
import com.example.e_faktura.ui.navigation.Screen
import com.example.e_faktura.ui.navigation.bottomNavItems
import com.example.e_faktura.ui.theme.EfakturaTheme

class MainActivity : ComponentActivity() {

    private lateinit var networkReceiver: NetworkChangeReceiver //
    private var isNetworkAvailable = mutableStateOf(true) //

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prośba o uprawnienia do powiadomień
        checkNotificationPermission()

        val syncIntent = Intent(this, SyncService::class.java) //
        startService(syncIntent) //

        networkReceiver = NetworkChangeReceiver { isOnline ->
            isNetworkAvailable.value = isOnline //
        }

        enableEdgeToEdge()
        setContent {
            EfakturaTheme {
                val rootNavController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) {
                    Column {
                        if (!isNetworkAvailable.value) { //
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.WifiOff, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "BRAK INTERNETU - TRYB OFFLINE",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        // Główny NavHost aplikacji
                        NavHost(navController = rootNavController, startDestination = Screen.Splash.route) {
                            composable(Screen.Splash.route) { SplashScreen(navController = rootNavController) }
                            composable(Screen.MainApp.route) { AppScaffold(rootNavController = rootNavController) }
                            composable(Screen.Login.route) { LoginScreen(navController = rootNavController) }
                            composable(Screen.Register.route) { RegistrationScreen(navController = rootNavController) }
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter) //
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver) //
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val user by authViewModel.user.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Automatyczny powrót do logowania po wylogowaniu
    LaunchedEffect(user) {
        if (user == null) {
            rootNavController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val rootRoutes = remember { bottomNavItems.map { it.route } }

    val isFullScreenRoute = currentRoute == Screen.AddCompany.route
            || currentRoute == Screen.AddInvoice.route
            || currentRoute?.startsWith("company_details/") == true
            || currentRoute?.startsWith("invoice_details/") == true
            || currentRoute == "account"

    val showGlobalBars = currentRoute in rootRoutes && !isFullScreenRoute
    val isLoggedIn = user != null && !user!!.isAnonymous

    Scaffold(
        topBar = {
            if (showGlobalBars) {
                TopAppBar(
                    title = {
                        val userDisplayName = user?.displayName ?: "Gość"
                        Text(if (isLoggedIn) "Witaj, $userDisplayName" else "Witaj, Gościu")
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) { Icon(Icons.Default.MoreVert, "Menu") }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (isLoggedIn) {
                                DropdownMenuItem(text = { Text("Moje Konto") }, onClick = { showMenu = false; navController.navigate("account") })
                                DropdownMenuItem(text = { Text("Wyloguj") }, onClick = { showMenu = false; authViewModel.logout() })
                            } else {
                                DropdownMenuItem(text = { Text("Zaloguj") }, onClick = { rootNavController.navigate(Screen.Login.route); showMenu = false })
                            }
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
                }) { Icon(Icons.Default.Add, "Dodaj") }
            }
        },
        bottomBar = {
            if (showGlobalBars) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, null) },
                            label = { Text(screen.label!!) },
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
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { InvoiceDashboardScreen(navController = navController) }
            composable(Screen.Companies.route) { CompanyListScreen(navController = navController) }
            composable(Screen.Statistics.route) {
                StatisticsScreen(navController = navController, statisticsViewModel = viewModel(factory = AppViewModelProvider.Factory), onOverdueClick = {})
            }
            composable(Screen.AddCompany.route) { AddCompanyScreen(navController = navController) }
            composable(Screen.AddInvoice.route) { AddInvoiceScreen(navController = navController, onInvoiceAdded = { navController.popBackStack() }) }
            composable("account") { MyAccountScreen(navController = navController) }

            composable(Screen.QrScanner.route) {
                QrCodeScannerScreen(navController = navController, onQrCodeScanned = { scannedNip ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scannedNip", scannedNip)
                    navController.popBackStack()
                })
            }

            composable(
                route = "company_details/{companyId}",
                arguments = listOf(navArgument("companyId") { type = NavType.StringType })
            ) { CompanyDetailsScreen(navController = navController) }

            composable(
                route = "invoice_details/{invoiceId}",
                arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("invoiceId")
                InvoiceDetailsScreen(invoiceId = id, navController = navController)
            }
        }
    }
}