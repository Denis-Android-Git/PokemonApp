package com.example.pokemon.data

import com.example.pokemon.domain.models.Pokemon
import com.example.pokemon.domain.models.PokemonFilter
import com.example.pokemon.domain.models.PokemonSortOption

object PokemonUtils {
    const val POKEMON_PER_PAGE = 20
    const val TOTAL_POKEMON_COUNT = 1000

    fun calculateOffset(page: Int): Int {
        return page * POKEMON_PER_PAGE
    }

    fun calculateTotalPages(totalCount: Int): Int {
        return (totalCount + POKEMON_PER_PAGE - 1) / POKEMON_PER_PAGE
    }

    fun hasMorePages(currentPage: Int, totalPages: Int): Boolean {
        return currentPage < totalPages - 1
    }

    fun applyFiltersAndSort(pokemons: List<Pokemon>, filter: PokemonFilter): List<Pokemon> {
        var filtered = pokemons

        if (filter.selectedTypes.isNotEmpty()) {
            filtered = filtered.filter { pokemon ->
                val hasMatchingType = pokemon.types.any { pokemonType ->
                    filter.selectedTypes.any { selectedType ->
                        selectedType.name.lowercase() == pokemonType.lowercase()
                    }
                }
                hasMatchingType
            }
        }

        filtered = when (filter.sortOption) {
            PokemonSortOption.NUMBER -> if (filter.sortAscending) filtered.sortedBy { it.id } else filtered.sortedByDescending { it.id }
            PokemonSortOption.NAME -> if (filter.sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
            PokemonSortOption.HP -> if (filter.sortAscending) filtered.sortedBy { it.hp } else filtered.sortedByDescending { it.hp }
            PokemonSortOption.ATTACK -> if (filter.sortAscending) filtered.sortedBy { it.attack } else filtered.sortedByDescending { it.attack }
            PokemonSortOption.DEFENSE -> if (filter.sortAscending) filtered.sortedBy { it.defense } else filtered.sortedByDescending { it.defense }
        }

        return filtered
    }
}