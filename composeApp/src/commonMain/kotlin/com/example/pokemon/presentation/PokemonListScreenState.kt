package com.example.pokemon.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import com.example.pokemon.domain.models.PokemonFilter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PokemonListScreenState(
    onNavigateToFilter: () -> Unit,
    currentFilter: PokemonFilter,
    onRefresh: () -> Unit,
    viewModel: PokemonListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pagedPokemons = viewModel.pokemonsPagedData.collectAsLazyPagingItems()

    PokemonListScreen(
        onNavigateToFilter = onNavigateToFilter,
        currentFilter = currentFilter,
        onRefresh = onRefresh,
        uiState = uiState,
        onApplyFilter = {
            viewModel.applyFilter(it)
        },
        onLoadNextItems = {
            viewModel.loadNextItems()
        },
        onClearFilter = {
            viewModel.clearFilter()
        },
        onSearchQueryChange = {
            viewModel.onSearchQueryChange(it)
        },
        pagedPokemons = pagedPokemons
    )
}
