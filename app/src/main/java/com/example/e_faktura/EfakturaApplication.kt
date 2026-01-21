package com.example.e_faktura

import android.app.Application
import com.example.e_faktura.data.AppContainer
import com.example.e_faktura.data.DefaultAppContainer

class EfakturaApplication : Application() {
    /**
     * Instancja AppContainer używana przez resztę klas do uzyskiwania zależności.
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}