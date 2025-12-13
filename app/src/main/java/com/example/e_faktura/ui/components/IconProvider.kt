package com.example.e_faktura.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

object IconProvider {
    // Replacement map using only core icons
    val icons = mapOf(
        "Apartment" to Icons.Filled.Home,
        "Store" to Icons.Filled.Home,
        "Restaurant" to Icons.Filled.Star,
        "LocalShipping" to Icons.Filled.Star,
        "Computer" to Icons.Filled.Info,
        "Agriculture" to Icons.Filled.Star,
        "Build" to Icons.Filled.Build,
        "ShoppingCart" to Icons.Filled.ShoppingCart,
        "MonetizationOn" to Icons.Filled.Star,
        "Palette" to Icons.Filled.Star,
        "Handyman" to Icons.Filled.Build,
        "MedicalServices" to Icons.Filled.Info,
        "Business" to Icons.Filled.Home, // Replaced as per instructions
        "Person" to Icons.Filled.Person,
        "AccountBalance" to Icons.Filled.Info
    )

    fun getIcon(name: String): ImageVector {
        return icons[name] ?: Icons.Filled.Home // Fallback to a safe, core icon
    }
}