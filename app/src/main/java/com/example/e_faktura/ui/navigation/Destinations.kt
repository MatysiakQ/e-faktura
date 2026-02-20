package com.example.e_faktura.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String? = null,
    val icon: ImageVector? = null,
    val showBars: Boolean = false
) {
    // ─── Główne ekrany (bottom nav) ───────────────────────────────────────────
    object Home : Screen("home", "Pulpit", Icons.Filled.Dashboard, showBars = true)
    object Companies : Screen("companies", "Moje Firmy", Icons.Filled.Business, showBars = true)
    object Statistics : Screen("statistics", "Statystyki", Icons.Filled.PieChart, showBars = true)

    // ─── Operacje na danych ───────────────────────────────────────────────────
    object AddInvoice : Screen("add_invoice")
    object AddCompany : Screen("add_company")
    object EditCompany : Screen("edit_company")
    object EditInvoice : Screen("edit_invoice")
    object QrScanner : Screen("scan_qr")

    // ─── Ustawienia i konto ───────────────────────────────────────────────────
    object Settings : Screen("settings")
    object KsefSetup : Screen("ksef_setup")

    // ─── Auth ─────────────────────────────────────────────────────────────────
    object Login : Screen("login")
    object Register : Screen("register")

    // ─── Root ─────────────────────────────────────────────────────────────────
    object Splash : Screen("splash")
    object MainApp : Screen("main_app")
    object LoginFlow : Screen("login_flow")
}

val bottomNavItems = listOf(Screen.Home, Screen.Companies, Screen.Statistics)

fun String?.shouldShowBars(): Boolean {
    return when {
        this == null -> false
        this == Screen.Home.route -> true
        this == Screen.Companies.route -> true
        this == Screen.Statistics.route -> true
        else -> false
    }
}
