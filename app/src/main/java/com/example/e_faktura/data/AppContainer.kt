package com.example.e_faktura.data

import android.content.Context
import com.example.e_faktura.data.api.RetrofitClient
import com.example.e_faktura.data.ksef.KsefApiClient
import com.example.e_faktura.data.ksef.KsefAuthManager
import com.example.e_faktura.data.ksef.KsefRepository
import com.example.e_faktura.data.local.AppDatabase
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

interface AppContainer {
    val companyRepository: CompanyRepository
    val invoiceRepository: InvoiceRepository
    val gusRepository: GusRepository
    val ksefRepository: KsefRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override val companyRepository: CompanyRepository by lazy {
        CompanyRepository(database.companyDao(), firestore, firebaseAuth)
    }

    override val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(database.invoiceDao(), firestore, firebaseAuth)
    }

    override val gusRepository: GusRepository by lazy {
        GusRepository(RetrofitClient.gusService)
    }

    // ─── KSeF ─────────────────────────────────────────────────────────────────
    private val ksefAuthManager: KsefAuthManager by lazy {
        KsefAuthManager(context)
    }

    override val ksefRepository: KsefRepository by lazy {
        KsefRepository(
            apiService = KsefApiClient.service,
            authManager = ksefAuthManager
        )
    }
}
