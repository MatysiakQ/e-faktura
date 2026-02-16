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
import com.example.e_faktura.ui.ksef.KsefSetupViewModel
import com.example.e_faktura.ui.settings.SettingsViewModel

/**
 * Fabryka dostarczająca zależności do wszystkich ViewModeli.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Autoryzacja Firebase
        initializer { AuthViewModel() }

        // Statystyki
        initializer {
            StatisticsViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository
            )
        }

        // Formularz firmy (GUS)
        initializer {
            CompanyFormViewModel(
                companyRepository = efakturaApplication().container.companyRepository,
                gusRepository = efakturaApplication().container.gusRepository
            )
        }

        // Lista firm
        initializer {
            CompanyListViewModel(
                companyRepository = efakturaApplication().container.companyRepository
            )
        }

        // Szczegóły firmy
        initializer {
            CompanyDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                companyRepository = efakturaApplication().container.companyRepository
            )
        }

        // Lista faktur
        initializer {
            InvoiceListViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository,
                companyRepository = efakturaApplication().container.companyRepository
            )
        }

        // Szczegóły faktury
        initializer {
            InvoiceDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                invoiceRepository = efakturaApplication().container.invoiceRepository
            )
        }

        // Dodawanie nowej faktury
        initializer {
            InvoiceViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository,
                companyRepository = efakturaApplication().container.companyRepository,
                gusRepository = efakturaApplication().container.gusRepository
            )
        }

        // ─── KSeF ─────────────────────────────────────────────────────────
        initializer {
            KsefSetupViewModel(
                ksefRepository = efakturaApplication().container.ksefRepository
            )
        }

        // Ustawienia
        initializer {
            SettingsViewModel(efakturaApplication())
        }
    }
}

fun CreationExtras.efakturaApplication(): EfakturaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EfakturaApplication)
