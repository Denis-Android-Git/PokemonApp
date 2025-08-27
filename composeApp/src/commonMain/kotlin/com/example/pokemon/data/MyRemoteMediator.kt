package com.example.pokemon.data

import app.cash.paging.ExperimentalPagingApi
import app.cash.paging.LoadType
import app.cash.paging.PagingState
import app.cash.paging.RemoteMediator
import coil3.network.HttpException
import com.example.pokemon.domain.interfaces.PokemonDao
import com.example.pokemon.domain.interfaces.PokemonService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

@OptIn(ExperimentalPagingApi::class)
class MyRemoteMediator(
    private val pokemonDao: PokemonDao,
    private val networkService: PokemonService
) : RemoteMediator<Int, PokemonEntity>() {

    private var nextOffset: Int? = null

    @OptIn(ExperimentalPagingApi::class, ExperimentalPagingApi::class)
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {

        return try {

            val offset = when (loadType) {
                LoadType.REFRESH -> {
                    println("Mediator: Refreshing data")
                    0
                }

                LoadType.PREPEND -> {
                    println("Mediator: Prepending data")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    println("Mediator: Appending data, nextOffset: $nextOffset")
                    nextOffset ?: return MediatorResult.Success(endOfPaginationReached = true)

                }
            }

            println("Mediator: Loading page: ${offset / PokemonUtils.POKEMON_PER_PAGE}")

            val response = networkService.getPokemons(
                offset = offset,
                limit = PokemonUtils.POKEMON_PER_PAGE
            )
            nextOffset = PokemonUtils.extractOffsetFromUrl(response.next)
            println("Mediator: Next offset from API: $nextOffset")

            withContext(Dispatchers.IO) {
                println("Mediator: Inserting data for page: $offset")
//                if (loadType == LoadType.REFRESH) {
//                    pokemonDao.clearPokemons()
//                }
                val problematicPokemon = setOf("swinub", "piloswine", "mamoswine")
                val pokemonEntities = mutableListOf<PokemonEntity>()
                response.results.forEach { (name, _) ->
                    if (name in problematicPokemon) {
                        return@forEach
                    }
                    val pokemon = networkService.getPokemon(name)
                    pokemonEntities.add(pokemon.toEntity())
                }
                pokemonDao.insertAll(pokemonEntities)
            }
            println("Mediator: Page ${offset / PokemonUtils.POKEMON_PER_PAGE} loaded, results: ${response.results.size}, next: ${response.next}, endOfPagination: ${response.next == null}")
            MediatorResult.Success(
                endOfPaginationReached = response.next == null
            )

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}