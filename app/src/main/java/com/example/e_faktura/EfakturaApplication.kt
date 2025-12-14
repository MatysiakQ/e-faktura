package com.example.e_faktura

import android.app.Application
import com.example.e_faktura.data.AppContainer
import com.example.e_faktura.data.AppDataContainer

class EfakturaApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
