package com.example.pokemon.domain.models


data class PokemonListUiState(
    val pokemons: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isEmpty: Boolean = false,
    val hasFiltersApplied: Boolean = false
)
