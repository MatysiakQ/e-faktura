package com.example.e_faktura.ui.navigation

// Definicja wszystkich ekranów w aplikacji
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")         // Naprawia błąd 'Dashboard'
    object Business : Screen("business_list")      // Naprawia błąd 'Business' (Lista Firm)
    object MyAccount : Screen("my_account")
    object AddCompany : Screen("add_company")
    object EditCompany : Screen("edit_company")
    object AddInvoice : Screen("add_invoice")
    object InvoiceDashboard : Screen("invoice_dashboard")
    object QrScanner : Screen("qr_scanner")
}