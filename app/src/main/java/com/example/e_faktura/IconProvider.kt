package com.example.e_faktura

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

object IconProvider {
    val icons = mapOf(
        "Apartment" to Icons.Filled.Apartment,
        "Store" to Icons.Filled.Store,
        "Restaurant" to Icons.Filled.Restaurant,
        "LocalShipping" to Icons.Filled.LocalShipping,
        "Computer" to Icons.Filled.Computer,
        "Agriculture" to Icons.Filled.Agriculture,
        "Build" to Icons.Filled.Build,
        "ShoppingCart" to Icons.Filled.ShoppingCart,
        "MonetizationOn" to Icons.Filled.MonetizationOn,
        "Palette" to Icons.Filled.Palette,
        "Handyman" to Icons.Filled.Handyman,
        "MedicalServices" to Icons.Filled.MedicalServices,
        "Business" to Icons.Filled.Business,
        "Person" to Icons.Filled.Person,
        "AccountBalance" to Icons.Filled.AccountBalance
    )

    fun getIcon(name: String): ImageVector {
        return icons[name] ?: Icons.Filled.Business
    }
}