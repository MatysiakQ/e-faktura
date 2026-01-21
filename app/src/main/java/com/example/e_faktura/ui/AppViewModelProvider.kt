package com.example.e_faktura.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.e_faktura.EfakturaApplication
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.company.details.CompanyDetailsViewModel
import com.example.e_faktura.ui.company.list.CompanyListViewModel
import com.example.e_faktura.ui.dashboard.StatisticsViewModel
import com.example.e_faktura.ui.invoice.add.InvoiceViewModel
import com.example.e_faktura.ui.invoice.details.InvoiceDetailsViewModel
import com.example.e_faktura.ui.invoice.list.InvoiceListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { AuthViewModel() }

        initializer {
            CompanyListViewModel(efakturaApplication().container.companyRepository)
        }

        initializer {
            CompanyFormViewModel(
                efakturaApplication().container.companyRepository,
                efakturaApplication().container.gusRepository
            )
        }

        // ✅ ZSYNCHRONIZOWANO: 3 parametry (Invoice, Company, Gus)
        initializer {
            InvoiceViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository,
                companyRepository = efakturaApplication().container.companyRepository,
                gusRepository = efakturaApplication().container.gusRepository
            )
        }

        initializer {
            InvoiceListViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository,
                companyRepository = efakturaApplication().container.companyRepository
            )
        }

        initializer {
            StatisticsViewModel(efakturaApplication().container.invoiceRepository)
        }

        initializer {
            InvoiceDetailsViewModel(
                this.createSavedStateHandle(),
                efakturaApplication().container.invoiceRepository
            )
        }

        initializer {
            CompanyDetailsViewModel(
                this.createSavedStateHandle(),
                efakturaApplication().container.companyRepository
            )
        }
    }
}

fun CreationExtras.efakturaApplication(): EfakturaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EfakturaApplication)