package com.example.pokemon.di

import com.example.pokemon.data.getDatabaseBuilder
import com.example.pokemon.data.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single {
        getRoomDatabase(getDatabaseBuilder(androidContext())).pokemonDao()
    }
}