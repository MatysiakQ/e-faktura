package com.example.e_faktura.di

import android.content.Context
import androidx.room.Room
import com.example.e_faktura.data.dao.CompanyDao
import com.example.e_faktura.data.dao.InvoiceDao
import com.example.e_faktura.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "efaktura_database"
        )
            // fallbackToDestructiveMigration() sprawia, że jak zmienisz model danych,
            // aplikacja wyczyści bazę zamiast się wysypać (dobre na etapie deweloperskim)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCompanyDao(database: AppDatabase): CompanyDao {
        return database.companyDao()
    }

    @Provides
    fun provideInvoiceDao(database: AppDatabase): InvoiceDao {
        return database.invoiceDao()
    }
}