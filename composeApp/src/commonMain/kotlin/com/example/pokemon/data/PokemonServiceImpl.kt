package com.example.pokemon.data

import com.example.pokemon.domain.interfaces.PokemonService
import com.example.pokemon.domain.models.PokemonDetailResponse
import com.example.pokemon.domain.models.PokemonResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class PokemonServiceImpl : PokemonService {
    private val httpClient = HttpClient {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP call $message")

                }
            }
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun getPokemon(idOrName: String): PokemonDetailResponse {
        return httpClient.get("https://pokeapi.co/api/v2/pokemon/$idOrName/").body()
    }

    override suspend fun getPokemons(offset: Int, limit: Int): PokemonResponse {
        return httpClient.get("https://pokeapi.co/api/v2/pokemon?offset=$offset&limit=$limit").body()
    }
}
