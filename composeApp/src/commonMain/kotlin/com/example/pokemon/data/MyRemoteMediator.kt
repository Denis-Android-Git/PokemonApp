package com.example.pokemon.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
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

    private var currentPage = 0

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {

        return try {
            when (loadType) {
                LoadType.REFRESH -> {
                    currentPage = 0
                }

                LoadType.PREPEND ->
                    return MediatorResult.Success(endOfPaginationReached = true)

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    currentPage++
                }
            }

            val offset = PokemonUtils.calculateOffset(currentPage)
            val response = networkService.getPokemons(
                offset = offset,
                limit = PokemonUtils.POKEMON_PER_PAGE
            )

            withContext(Dispatchers.IO) {
                if (loadType == LoadType.REFRESH) {
                    pokemonDao.clearPokemons()
                }
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

            MediatorResult.Success(
                endOfPaginationReached = response.results.isEmpty()
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}