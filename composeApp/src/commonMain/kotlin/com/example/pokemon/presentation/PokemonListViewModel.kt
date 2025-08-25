package com.example.pokemon.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemon.data.PokemonRepository
import com.example.pokemon.data.PokemonUtils
import com.example.pokemon.data.toPokemon
import com.example.pokemon.domain.models.PokemonFilter
import com.example.pokemon.domain.models.PokemonListUiState
import com.example.pokemon.domain.models.PokemonSortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 0
    private var currentFilter: PokemonFilter = PokemonFilter()
    private var hasMorePages = true
    private var totalPokemonCount = 0

    private val pageLimit = 20

    private val paginator = Paginator(
        initialKey = 0,
        onLoadUpdated = { loading ->
            println("loadNextItems_vm: paginator onLoadUpdated")
            _uiState.update {
                it.copy(isLoading = loading)
            }
        },
        onRequest = { currentPage ->
            println("loadNextItems_vm: paginator onRequest")

            val offset = PokemonUtils.calculateOffset(currentPage)

            pokemonRepository.fetchAndCachePokemons(offset, pageLimit)
        },
        getNextKey = { currentPage, _ ->
            println("loadNextItems_vm: paginator getNextKey")

            currentPage + 1
        },
        onError = {
            println("loadNextItems_vm: paginator onError")

            _uiState.update {
                it.copy(error = it.error ?: "Unknown error")
            }
        },
        onSuccess = { response, nextPage ->
            println("loadNextItems_vm: paginator onSuccess")

            _uiState.update { state ->
                state.copy(
                    pokemons = state.pokemons + response.results.map { it.toPokemon() },
                    error = null,
                    isLoading = false,
                    isEmpty = response.results.isEmpty(),
                    hasFiltersApplied = false
                )
            }
        },
        endReached = { currentPage, response ->
            println("loadNextItems_vm: paginator endReached")
            (currentPage * pageLimit >= response.count)

        }
    )

    init {
        println("loadNextItems_vm: viewModel init!!!")
        loadNextItems()
    }

    fun loadNextItems() {
        viewModelScope.launch {
            try {
                paginator.loadNextItems()
                println("loadNextItems_vm: Loading next items")
            } catch (_: Exception) {
                println("loadNextItems_vm: Error loading next items")
                pokemonRepository.getPokemonsWithoutPagination()
                    .stateIn(
                        viewModelScope,
                        started = SharingStarted.WhileSubscribed(),
                        initialValue = emptyList()
                    ).collect { pokemons ->
                        _uiState.update {
                            it.copy(
                                pokemons = pokemons,
                                error = null,
                                isLoading = false,
                                isEmpty = pokemons.isEmpty(),
                                hasFiltersApplied = false
                            )
                        }
                    }
            }
        }
    }

    fun clearFilter() {
        viewModelScope.launch {
            currentFilter = PokemonFilter()
        }
    }

    private suspend fun updatePaginationState() {
        try {
            totalPokemonCount = pokemonRepository.getPokemonCount(_uiState.value.searchQuery)
            val totalPages = PokemonUtils.calculateTotalPages(totalPokemonCount)
            hasMorePages = PokemonUtils.hasMorePages(currentPage, totalPages)
            if (_uiState.value.searchQuery.isNotBlank() && totalPokemonCount == 0) {
                hasMorePages = false
                println("Search query active but no results found, setting hasMorePages=false")
            }

            if (_uiState.value.searchQuery.isBlank()) {
                val expectedPokemonsForNextPage = (currentPage + 1) * PokemonUtils.POKEMON_PER_PAGE
                val totalPokemonInApi = PokemonUtils.TOTAL_POKEMON_COUNT

                if (totalPokemonInApi > expectedPokemonsForNextPage) {
                    hasMorePages = true
                    println("More pokemons available in API ($totalPokemonInApi > $expectedPokemonsForNextPage), setting hasMorePages=true")
                } else {
                    hasMorePages = false
                    println("Reached end of available pokemons in API ($totalPokemonInApi <= $expectedPokemonsForNextPage), setting hasMorePages=false")
                }
            }

        } catch (e: Exception) {
            println("Error updating pagination state: ${e.message}")
            val currentSize = _uiState.value.pokemons.size
            hasMorePages = currentSize >= PokemonUtils.POKEMON_PER_PAGE
            println("Pagination Fallback: currentSize=$currentSize, hasMorePages=$hasMorePages")
        }
    }

    fun onSearchQueryChange(query: String) {
        println("ViewModel: onSearchQueryChange called with query: '$query'")
        _uiState.value = _uiState.value.copy(searchQuery = query)
        currentPage = 0
        hasMorePages = true

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val offset = PokemonUtils.calculateOffset(currentPage)
                val limit = PokemonUtils.POKEMON_PER_PAGE

                println("Searching with query: '$query', offset=$offset, limit=$limit")

                val pokemons = pokemonRepository.getPokemons(
                    offset,
                    limit,
                    query
                ).first()

                println("Search returned ${pokemons.size} pokemons for query: '$query'")

                val filteredAndSorted = PokemonUtils.applyFiltersAndSort(pokemons, currentFilter)

                _uiState.value = _uiState.value.copy(
                    pokemons = filteredAndSorted,
                    isLoading = false,
                    isEmpty = filteredAndSorted.isEmpty(),
                    hasFiltersApplied = false
                )

                println("Search completed. Total pokemons: ${filteredAndSorted.size}")

                updatePaginationState()

            } catch (e: Exception) {
                println("Error during search: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun applyFilter(filter: PokemonFilter) {
        if (filter.selectedTypes.isNotEmpty() || filter.sortOption != PokemonSortOption.NUMBER) {
            println("Applying filter: $filter")
            currentFilter = filter
            currentPage = 0
            hasMorePages = false

            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null,
                        pokemons = emptyList()
                    )

                    val totalCount = pokemonRepository.getPokemonCount(_uiState.value.searchQuery)
                    val pokemons = pokemonRepository.getPokemons(
                        offset = 0,
                        limit = totalCount,
                        search = _uiState.value.searchQuery
                    ).first()

                    val filteredAndSorted = PokemonUtils.applyFiltersAndSort(pokemons, filter)

                    _uiState.value = _uiState.value.copy(
                        pokemons = filteredAndSorted,
                        isLoading = false,
                        isEmpty = filteredAndSorted.isEmpty(),
                        hasFiltersApplied = filter.selectedTypes.isNotEmpty() || filter.sortOption != PokemonSortOption.NUMBER || !filter.sortAscending
                    )
                    updatePaginationState()

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}