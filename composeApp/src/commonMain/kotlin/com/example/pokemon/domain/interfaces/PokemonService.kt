package com.example.pokemon.domain.interfaces

import com.example.pokemon.domain.models.PokemonDetailResponse
import com.example.pokemon.domain.models.PokemonResponse

interface PokemonService {
    suspend fun getPokemon(idOrName: String): PokemonDetailResponse
    suspend fun getPokemons(offset: Int, limit: Int): PokemonResponse
}