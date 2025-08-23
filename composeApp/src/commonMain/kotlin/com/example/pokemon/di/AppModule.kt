package com.example.pokemon.di

import com.example.pokemon.data.PokemonRepository
import com.example.pokemon.data.PokemonRepositoryImpl
import com.example.pokemon.domain.interfaces.PokemonService
import com.example.pokemon.data.PokemonServiceImpl
import com.example.pokemon.presentation.PokemonListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single<PokemonService> { PokemonServiceImpl() }

    singleOf(::PokemonRepositoryImpl).bind<PokemonRepository>()

    viewModelOf(::PokemonListViewModel)
}
