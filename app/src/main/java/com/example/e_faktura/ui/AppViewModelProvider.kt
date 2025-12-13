package com.example.e_faktura.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.e_faktura.EfakturaApplication
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.company.CompanyViewModel
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.invoice.InvoiceViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(efakturaApplication().container.authRepository)
        }

        initializer {
            InvoiceViewModel(
                efakturaApplication().container.invoiceRepository,
                efakturaApplication().container.gusRepository,
                efakturaApplication().container.companyRepository
            )
        }

        initializer {
            CompanyViewModel(efakturaApplication().container.companyRepository)
        }

        initializer {
            CompanyFormViewModel(
                efakturaApplication().container.companyRepository,
                efakturaApplication().container.gusRepository
            )
        }
    }
}

fun CreationExtras.efakturaApplication(): EfakturaApplication = 
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EfakturaApplication)
