package com.example.e_faktura.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.e_faktura.EfakturaApplication
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.company.details.CompanyDetailsViewModel
import com.example.e_faktura.ui.company.list.CompanyListViewModel
import com.example.e_faktura.ui.invoice.add.InvoiceViewModel
import com.example.e_faktura.ui.invoice.list.InvoiceListViewModel
import androidx.lifecycle.createSavedStateHandle

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel()
        }

        initializer {
            CompanyListViewModel(efakturaApplication().container.companyRepository)
        }

        initializer {
            CompanyDetailsViewModel(
                this.createSavedStateHandle(),
                efakturaApplication().container.companyRepository
            )
        }

        initializer {
            CompanyFormViewModel(
                efakturaApplication().container.companyRepository,
                efakturaApplication().container.gusRepository
            )
        }

        initializer {
            InvoiceViewModel(
                efakturaApplication().container.invoiceRepository
            )
        }

        initializer {
            InvoiceListViewModel(
                efakturaApplication().container.invoiceRepository,
                efakturaApplication().container.companyRepository
            )
        }
    }
}

fun CreationExtras.efakturaApplication(): EfakturaApplication = 
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EfakturaApplication)