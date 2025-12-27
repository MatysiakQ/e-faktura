package com.example.e_faktura.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.company.list.CompanyListScreen
import com.example.e_faktura.ui.invoice.list.InvoiceDashboardScreen
import com.example.e_faktura.ui.invoice.list.InvoiceListScreen

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    var showMenu by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem("pulpit", "Pulpit", Icons.Default.Dashboard),
        BottomNavItem("faktury", "Faktury", Icons.Default.Receipt),
        BottomNavItem("firmy", "Firmy", Icons.Default.Business)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("E-Faktura") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Moje Konto") },
                            onClick = {
                                rootNavController.navigate("my_account")
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Wyloguj") },
                            onClick = {
                                authViewModel.logout()
                                rootNavController.navigate("login") {
                                    popUpTo(rootNavController.graph.id) { inclusive = true }
                                }
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "pulpit",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("pulpit") {
                InvoiceDashboardScreen(navController = rootNavController)
            }
            composable("faktury") {
                InvoiceListScreen(navController = rootNavController)
            }
            composable("firmy") {
                CompanyListScreen(navController = rootNavController)
            }
        }
    }
}
