package com.example.e_faktura.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UNIFIED data model for a company.
 * This is the single source of truth for all company-related data in the app.
 */
@Parcelize
data class Company(
    val id: String = "",
    val nip: String = "",
    val businessName: String = "",
    val address: String = "",
    val ownerFullName: String = "",
    val bankAccount: String = "",
    val type: String = "",
    val icon: String = ""
): Parcelable
