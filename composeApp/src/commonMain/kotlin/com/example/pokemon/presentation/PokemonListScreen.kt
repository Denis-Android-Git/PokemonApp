package com.example.pokemon.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.pokemon.domain.models.Pokemon
import com.example.pokemon.domain.models.PokemonFilter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onNavigateToFilter: () -> Unit,
    currentFilter: PokemonFilter,
    viewModel: PokemonListViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(currentFilter) {
        println("Screen: Filter changed to: $currentFilter")
        viewModel.applyFilter(currentFilter)
    }

    LaunchedEffect(lazyGridState, uiState.hasFiltersApplied) {
        if (!uiState.hasFiltersApplied) {
            snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->
                    val lastVisibleItem = visibleItems.lastOrNull()
                    if (lastVisibleItem != null && uiState.pokemons.isNotEmpty() && viewModel.canLoadMore()) {
                        val threshold = uiState.pokemons.size - 4
                        if (lastVisibleItem.index >= threshold) {
                            viewModel.loadNextPage()
                        }
                    }
                }
        }
    }

    LaunchedEffect(uiState) {
        println("Loading state changed to: ${uiState.isLoading}, isEmpty: ${uiState.isEmpty}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Покемоны") },
                actions = {
                    IconButton(onClick = { viewModel.reloadData() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Перезагрузить данные"
                        )
                    }
                    IconButton(onClick = onNavigateToFilter) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Фильтр"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            isRefreshing = uiState.isLoading,
            onRefresh = {
                viewModel.clearFilter()
                viewModel.loadPokemons()
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { query ->
                        println("Screen: Search query changed to: '$query'")
                        viewModel.onSearchQueryChange(query)
                    },
                    label = { Text("Поиск покемонов") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState.isEmpty) {
                    Text("Покемоны не найдены", modifier = Modifier.padding(16.dp))
                } else if (uiState.error != null) {
                    val errorMessage = when (uiState.error) {
                        else -> "Ошибка: ${uiState.error}"
                    }
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.pokemons) { pokemon ->
                            PokemonCard(pokemon = pokemon)
                        }

                        if (viewModel.canLoadMore() && uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonCard(pokemon: Pokemon) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(pokemon.name, style = MaterialTheme.typography.bodyLarge)
            if (pokemon.hp > 0 || pokemon.attack > 0 || pokemon.defense > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "HP: ${pokemon.hp} | ATK: ${pokemon.attack} | DEF: ${pokemon.defense}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (pokemon.types.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    pokemon.types.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
