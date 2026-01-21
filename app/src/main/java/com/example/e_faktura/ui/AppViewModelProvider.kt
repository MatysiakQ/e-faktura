package com.example.e_faktura.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.e_faktura.EfakturaApplication
import com.example.e_faktura.ui.auth.AuthViewModel
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.company.list.CompanyListViewModel
import com.example.e_faktura.ui.dashboard.StatisticsViewModel
import com.example.e_faktura.ui.invoice.add.InvoiceViewModel
import com.example.e_faktura.ui.invoice.details.InvoiceDetailsViewModel
import com.example.e_faktura.ui.invoice.list.InvoiceListViewModel

/**
 * Dostarcza fabrykę do tworzenia wszystkich ViewModeli w aplikacji.
 * Eliminuje potrzebę korzystania z biblioteki Hilt.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Autoryzacja
        initializer { AuthViewModel() }

        // Statystyki
        initializer {
            StatisticsViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository
            )
        }

        // Formularz Dodawania/Edycji Firmy
        initializer {
            CompanyFormViewModel(
                companyRepository = efakturaApplication().container.companyRepository,
                gusRepository = efakturaApplication().container.gusRepository
            )
        }

        // Lista Firm
        initializer {
            CompanyListViewModel(
                companyRepository = efakturaApplication().container.companyRepository
            )
        }

        // Lista Faktur
        initializer {
            InvoiceListViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository,
                companyRepository = efakturaApplication().container.companyRepository
            )
        }

        // Szczegóły Faktury - wymaga SavedStateHandle do odczytu ID z URL
        initializer {
            InvoiceDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                invoiceRepository = efakturaApplication().container.invoiceRepository
            )
        }

        // Dodawanie Faktury
        initializer {
            InvoiceViewModel(
                invoiceRepository = efakturaApplication().container.invoiceRepository,
                companyRepository = efakturaApplication().container.companyRepository,
                gusRepository = efakturaApplication().container.gusRepository
            )
        }
    }
}

/**
 * Rozszerzenie ułatwiające dostęp do instancji aplikacji i kontenera danych.
 */
fun CreationExtras.efakturaApplication(): EfakturaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EfakturaApplication)