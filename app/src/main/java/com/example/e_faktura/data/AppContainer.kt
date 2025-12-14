package com.example.e_faktura.data

import android.content.Context
import com.example.e_faktura.data.api.GusService
import com.example.e_faktura.data.local.AppDatabase
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val companyRepository: CompanyRepository
    val invoiceRepository: InvoiceRepository
    val gusRepository: GusRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Create Retrofit service for GUS API
    private val gusService: GusService by lazy {
        Retrofit.Builder()
            .baseUrl("https://your-gus-api-base-url.com/") // <-- IMPORTANT: Replace with actual API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GusService::class.java)
    }
    
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val companyRepository: CompanyRepository by lazy {
        CompanyRepository(
            companyDao = database.companyDao(),
            firestore = firestore,
            firebaseAuth = firebaseAuth
        )
    }

    override val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(
            invoiceDao = database.invoiceDao(),
            firestore = firestore,
            firebaseAuth = firebaseAuth
        )
    }

    override val gusRepository: GusRepository by lazy {
        GusRepository(gusService = gusService)
    }
}
