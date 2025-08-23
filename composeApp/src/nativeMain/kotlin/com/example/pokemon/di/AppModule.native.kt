package com.example.pokemon.di

import com.example.pokemon.data.database.getDatabaseBuilder
import com.example.pokemon.data.getRoomDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single {
        //createHttpClient(Darwin.create())
    }

    single {
        getRoomDatabase(getDatabaseBuilder()).pokemonDao()
    }
}