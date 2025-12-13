package com.example.e_faktura // RENAMED

import android.app.Application
import com.example.e_faktura.data.AppContainer
import com.example.e_faktura.data.DefaultAppContainer

class EfakturaApplication : Application() { // RENAMED
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
