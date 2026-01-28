package com.example.e_faktura.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    // Main App
    object Home : Screen("home", "Pulpit", Icons.Filled.Dashboard)
    object Companies : Screen("companies", "Moje Firmy", Icons.Filled.Business)
    object Statistics : Screen("statistics", "Statystyki", Icons.Filled.PieChart)
    object AddInvoice : Screen("add_invoice")
    object AddCompany : Screen("add_company")
    object QrScanner : Screen("scan_qr")

    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Nav
    object Splash : Screen("splash")
    object MainApp : Screen("main_app")
    object LoginFlow : Screen("login_flow")
}

val bottomNavItems = listOf(Screen.Home, Screen.Companies)
