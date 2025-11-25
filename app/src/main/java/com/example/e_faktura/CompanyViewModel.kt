package com.example.e_faktura

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CompanyViewModel : ViewModel() {
    var companyType by mutableStateOf(CompanyType.FIRM)
    var ownerFullName by mutableStateOf("")
    var businessName by mutableStateOf("")
    var nip by mutableStateOf("")
    var address by mutableStateOf("")
    var icon by mutableStateOf(CompanyIcon(IconType.PREDEFINED, "Business"))

    fun onCompanyTypeChange(newType: CompanyType) {
        companyType = newType
    }

    fun onOwnerFullNameChange(newOwnerFullName: String) {
        ownerFullName = newOwnerFullName
    }

    fun onBusinessNameChange(newBusinessName: String) {
        businessName = newBusinessName
    }

    fun onNipChange(newNip: String) {
        nip = newNip
    }

    fun onAddressChange(newAddress: String) {
        address = newAddress
    }

    fun onIconChange(newIcon: CompanyIcon) {
        icon = newIcon
    }

    fun saveCompany(): Company {
        return Company(
            type = companyType,
            nip = nip,
            address = address,
            ownerFullName = if (companyType == CompanyType.SOLE_PROPRIETORSHIP) ownerFullName else null,
            businessName = if (companyType == CompanyType.FIRM) businessName else null,
            icon = icon
        )
    }
}