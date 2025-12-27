package com.example.e_faktura.di

import com.example.e_faktura.data.api.GusService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Adres bazowy API. Jeśli nie masz prawdziwego, wpisz cokolwiek,
    // aplikacja się skompiluje, ale funkcja pobierania z GUS nie zwróci danych.
    private const val BASE_URL = "https://api.stat.gov.pl/v1/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGusService(retrofit: Retrofit): GusService {
        return retrofit.create(GusService::class.java)
    }
}