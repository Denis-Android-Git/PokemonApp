package com.example.pokemon.data

import com.example.pokemon.domain.models.NamedApiResource
import com.example.pokemon.domain.models.PokemonDetailResponse
import com.example.pokemon.domain.models.Pokemon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PokemonRepositoryImpl(
    private val pokemonService: PokemonService,
    private val pokemonDao: PokemonDao
) : PokemonRepository {

    override fun getPokemons(offset: Int, limit: Int, search: String?): Flow<List<Pokemon>> {
        return if (search.isNullOrBlank()) {
            pokemonDao.getPokemonsWithPagination(limit, offset).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            pokemonDao.searchPokemonsWithPagination("%$search%", limit, offset).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun fetchAndCachePokemons(offset: Int, limit: Int) {
        val response = pokemonService.getPokemons(offset, limit)

        val problematicPokemon = setOf("swinub", "piloswine", "mamoswine")

        val pokemonEntities = mutableListOf<PokemonEntity>()

        response.results.forEachIndexed { index, namedApiResource ->
            try {
                if (namedApiResource.name in problematicPokemon) {
                    return@forEachIndexed
                }
                
                val detail = pokemonService.getPokemon(namedApiResource.name)
                val entity = detail.toEntity()
                pokemonEntities.add(entity)
            } catch (e: Exception) {
                if (e.message?.contains("JSON") == true) {
                    try {
                        val fallbackEntity = namedApiResource.toEntity()
                        pokemonEntities.add(fallbackEntity)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        pokemonDao.insertAll(pokemonEntities)
    }

    override suspend fun getPokemonCount(): Int {
        val count = pokemonDao.getPokemonCount()
        return count
    }

    override suspend fun getPokemonCount(search: String?): Int {
        val count = if (search.isNullOrBlank()) {
            pokemonDao.getPokemonCount()
        } else {
            pokemonDao.getPokemonCountWithSearch("%$search%")
        }
        return count
    }

    override suspend fun clearCachedPokemons() {
        pokemonDao.clearPokemons()
    }
}

interface PokemonRepository {
    fun getPokemons(offset: Int, limit: Int, search: String? = null): Flow<List<Pokemon>>
    suspend fun fetchAndCachePokemons(offset: Int, limit: Int)
    suspend fun getPokemonCount(): Int
    suspend fun getPokemonCount(search: String?): Int
    suspend fun clearCachedPokemons()
}

fun NamedApiResource.toEntity(): PokemonEntity {
    val id = url.split("/").dropLast(1).last().toInt()
    
    val fallbackTypes = when {
        id <= 151 -> "normal"
        else -> ""
    }
    
    return PokemonEntity(
        id = id,
        name = name,
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
        hp = 50,
        attack = 50,
        defense = 50,
        types = fallbackTypes
    )
}

fun PokemonDetailResponse.toEntity(): PokemonEntity {
    val hp = stats.find { it.stat.name == "hp" }?.base_stat ?: 0
    val attack = stats.find { it.stat.name == "attack" }?.base_stat ?: 0
    val defense = stats.find { it.stat.name == "defense" }?.base_stat ?: 0
    val typesString = types.joinToString(",") {
        it.type.name }

    return PokemonEntity(
        id = id,
        name = name,
        imageUrl = sprites.front_default ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
        hp = hp,
        attack = attack,
        defense = defense,
        types = typesString
    )
}

fun PokemonEntity.toDomain(): Pokemon {
    val typesList = if (types.isNotEmpty()) types.split(",") else emptyList()

    return Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        hp = hp,
        attack = attack,
        defense = defense,
        types = typesList
    )
}
