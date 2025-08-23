package com.example.pokemon.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemon.data.PokemonRepository
import com.example.pokemon.domain.models.Pokemon
import com.example.pokemon.domain.models.PokemonFilter
import com.example.pokemon.data.PokemonUtils
import com.example.pokemon.domain.models.PokemonSortOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var currentFilter: PokemonFilter = PokemonFilter()
    private var isLoadingMore = false
    private var hasMorePages = true
    private var totalPokemonCount = 0

    init {
        loadPokemons()
    }

    fun clearFilter() {
        viewModelScope.launch {
            currentFilter = PokemonFilter()
        }
    }

    fun loadPokemons() {
        if (isLoadingMore) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            delay(100)
            try {
                currentPage = 0
                hasMorePages = true

                val existingCount = pokemonRepository.getPokemonCount()
                println("Existing pokemons in DB: $existingCount")

                if (existingCount == 0) {
                    println("No data in DB, loading from API")
                    val initialLimit = 60
                    pokemonRepository.fetchAndCachePokemons(0, initialLimit)
                    val countAfterLoad = pokemonRepository.getPokemonCount()
                    println("After API fetch, DB contains $countAfterLoad pokemons")
                } else {
                    println("Using cached data from DB")
                }

                val offset = PokemonUtils.calculateOffset(currentPage)
                val limit = PokemonUtils.POKEMON_PER_PAGE

                println("Loading from DB: page $currentPage with offset=$offset, limit=$limit, search='${_uiState.value.searchQuery}'")

                val pokemons = pokemonRepository.getPokemons(
                    offset,
                    limit,
                    _uiState.value.searchQuery
                ).first()

                println("Received ${pokemons.size} pokemons for page $currentPage")

                val filteredAndSorted = PokemonUtils.applyFiltersAndSort(pokemons, currentFilter)

                _uiState.value = _uiState.value.copy(
                    pokemons = filteredAndSorted,
                    isLoading = false,
                    isEmpty = filteredAndSorted.isEmpty(),
                    hasFiltersApplied = false
                )

                println("Page $currentPage loaded successfully. Total pokemons: ${filteredAndSorted.size}")

                updatePaginationState()

            } catch (e: Exception) {
                println("Error_loading_Pokemons: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
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

    fun loadNextPage() {
        println("loadNextPage called: isLoadingMore=$isLoadingMore, hasMorePages=$hasMorePages, currentPage=$currentPage")
        if (!isLoadingMore && hasMorePages) {
            currentPage++
            println("Loading next page: $currentPage")

            viewModelScope.launch {
                try {
                    isLoadingMore = true

                    val offset = PokemonUtils.calculateOffset(currentPage)
                    val limit = PokemonUtils.POKEMON_PER_PAGE

                    println("Loading next page with offset=$offset, limit=$limit, search='${_uiState.value.searchQuery}'")

                    val currentCount = pokemonRepository.getPokemonCount(_uiState.value.searchQuery)
                    val expectedCountForPage = (currentPage + 1) * PokemonUtils.POKEMON_PER_PAGE

                    println("DB check: currentCount=$currentCount, expectedCountForPage=$expectedCountForPage")

                    if (currentCount < expectedCountForPage && _uiState.value.searchQuery.isBlank()) {
                        println("Not enough data in DB ($currentCount < $expectedCountForPage), trying to fetch from API")
                        try {
                            pokemonRepository.fetchAndCachePokemons(offset, limit)

                            delay(500)

                            val countAfterLoad = pokemonRepository.getPokemonCount(_uiState.value.searchQuery)
                            println("After API fetch: DB contains $countAfterLoad pokemons")
                        } catch (e: Exception) {
                            println("Failed to fetch from API: ${e.message}, using cached data only")
                        }
                    }

                    val pokemonsFromDb = pokemonRepository.getPokemons(
                        offset,
                        limit,
                        _uiState.value.searchQuery
                    ).first()

                    println("Next page loaded from DB: ${pokemonsFromDb.size} pokemons")

                    if (pokemonsFromDb.isNotEmpty()) {
                        val filteredAndSorted = PokemonUtils.applyFiltersAndSort(
                            pokemonsFromDb,
                            currentFilter
                        )
                        val updatedList = _uiState.value.pokemons.toMutableList().apply {
                            addAll(
                                filteredAndSorted
                            )
                        }

                        _uiState.value = _uiState.value.copy(
                            pokemons = updatedList,
                            isEmpty = updatedList.isEmpty()
                        )

                    } else {
                        if (_uiState.value.searchQuery.isBlank()) {
                            try {
                                pokemonRepository.fetchAndCachePokemons(offset, limit)
                                delay(500)

                                val retryPokemons = pokemonRepository.getPokemons(
                                    offset,
                                    limit,
                                    _uiState.value.searchQuery
                                ).first()

                                if (retryPokemons.isNotEmpty()) {
                                    val filteredAndSorted = PokemonUtils.applyFiltersAndSort(
                                        retryPokemons,
                                        currentFilter
                                    )
                                    val updatedList = _uiState.value.pokemons.toMutableList().apply {
                                        addAll(
                                            filteredAndSorted
                                        )
                                    }

                                    _uiState.value = _uiState.value.copy(
                                        pokemons = updatedList,
                                        isEmpty = updatedList.isEmpty()
                                    )

                                }
                            } catch (e: Exception) {
                                println("API fallback failed: ${e.message}")
                            }
                        }
                    }

                    updatePaginationState()
                    isLoadingMore = false

                } catch (e: Exception) {
                    println("Error loading next page: ${e.message}")
                    isLoadingMore = false
                }
            }
        } else {
            println("Cannot load next page: isLoadingMore=$isLoadingMore, hasMorePages=$hasMorePages")
        }
    }

    fun applyFilter(filter: PokemonFilter) {
        if (filter.selectedTypes.isNotEmpty()) {
            println("Applying filter: $filter")
            currentFilter = filter
            currentPage = 0
            hasMorePages = false

            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

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

    fun canLoadMore(): Boolean {
        val result = !isLoadingMore && hasMorePages
        return result
    }

    fun reloadData() {
        println("Reloading data")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )
                currentPage = 0
                hasMorePages = true
                val initialLimit = 20
                pokemonRepository.fetchAndCachePokemons(0, initialLimit)

                val offset = PokemonUtils.calculateOffset(currentPage)
                val limit = PokemonUtils.POKEMON_PER_PAGE

                val pokemons = pokemonRepository.getPokemons(
                    offset,
                    limit,
                    _uiState.value.searchQuery
                ).first()

                _uiState.value = _uiState.value.copy(
                    pokemons = pokemons,
                    isLoading = false,
                    isEmpty = pokemons.isEmpty(),
                    hasFiltersApplied = false
                )

                updatePaginationState()

            } catch (e: Exception) {
                println("Error reloading data: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}

data class PokemonListUiState(
    val pokemons: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isEmpty: Boolean = false,
    val hasFiltersApplied: Boolean = false
)
