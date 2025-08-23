package com.example.pokemon.domain.models

data class PokemonFilter(
    val selectedTypes: Set<PokemonFilterType> = emptySet(),
    val sortOption: PokemonSortOption = PokemonSortOption.NUMBER,
    val sortAscending: Boolean = true
)

