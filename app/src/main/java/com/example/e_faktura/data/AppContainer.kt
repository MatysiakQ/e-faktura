package com.example.e_faktura.data

import android.content.Context
import com.example.e_faktura.data.api.GusService
import com.example.e_faktura.data.repository.AuthRepository
import com.example.e_faktura.data.repository.CompanyRepository
import com.example.e_faktura.data.repository.GusRepository
import com.example.e_faktura.data.repository.InvoiceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val authRepository: AuthRepository
    val companyRepository: CompanyRepository
    val invoiceRepository: InvoiceRepository
    val gusRepository: GusRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://wyszukiwarkaregon.stat.gov.pl/wsBIR/UslugaBIRzewnPubl.svc/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val gusService: GusService by lazy {
        retrofit.create(GusService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }

    override val companyRepository: CompanyRepository by lazy {
        CompanyRepository(firestore, firebaseAuth)
    }

    override val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(firestore, firebaseAuth)
    }

    override val gusRepository: GusRepository by lazy {
        GusRepository(gusService)
    }
}
