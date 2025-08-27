package com.example.pokemon.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.pokemon.data.PokemonEntity
import com.example.pokemon.data.toDomain
import com.example.pokemon.domain.models.Pokemon
import com.example.pokemon.domain.models.PokemonFilter
import com.example.pokemon.domain.models.PokemonListUiState
import com.example.pokemon.domain.models.PokemonSortOption
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onNavigateToFilter: () -> Unit,
    currentFilter: PokemonFilter,
    onRefresh: () -> Unit,
    uiState: PokemonListUiState,
    onApplyFilter: (PokemonFilter) -> Unit,
    //onLoadNextItems: () -> Unit,
    onClearFilter: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    pagedPokemons: LazyPagingItems<PokemonEntity>?
) {
    val lazyGridState = rememberLazyGridState()
    val isPagingLoading =
        pagedPokemons?.loadState?.refresh is androidx.paging.LoadState.Loading ||
                pagedPokemons?.loadState?.append is androidx.paging.LoadState.Loading

    println("isPagingLoading: $isPagingLoading")
    LaunchedEffect(currentFilter) {
        println("Screen: Filter changed to: $currentFilter")
        onApplyFilter(currentFilter)
    }

//    LaunchedEffect(uiState.pokemons) {
//        snapshotFlow {
//            lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
//        }
//            .distinctUntilChanged()
//            .collect { lastVisibleIndex ->
//                if (lastVisibleIndex == uiState.pokemons.lastIndex && currentFilter.selectedTypes.isEmpty() && uiState.searchQuery == "") {
//                    println("Screen: Reached end of list, loading more items")
//                    onLoadNextItems()
//                }
//            }
//    }

    LaunchedEffect(uiState) {
        println("Loading state changed to: ${uiState.isLoading}, isEmpty: ${uiState.isEmpty}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Покемоны") },
                actions = {
                    IconButton(onClick = {
                        onRefresh()
                    }) {
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
            isRefreshing = uiState.isLoading || isPagingLoading,
            onRefresh = {
                onClearFilter()
                onRefresh()
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
                        onSearchQueryChange(query)
                    },
                    label = { Text("Поиск покемонов") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                if (uiState.isEmpty) {
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
                        if (currentFilter.selectedTypes.isNotEmpty() || currentFilter.sortOption != PokemonSortOption.NUMBER
                            || uiState.searchQuery.isNotEmpty()
                        ) {
                            println("Screen: Filter changed to: NOT paged data $currentFilter")
                            items(uiState.pokemons) { pokemon ->
                                PokemonCard(pokemon = pokemon)
                            }
                        } else {
                            println("Screen: Filter changed to: paged data $currentFilter")
                            items(
                                pagedPokemons?.itemCount ?: 0,
                                key = {
                                    it
                                }
                            ) { index ->
                                pagedPokemons?.get(index)?.let { pokemon ->
                                    PokemonCard(pokemon = pokemon.toDomain())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PokemonListScreenPreview() {
    MaterialTheme {
        PokemonListScreen(
            onNavigateToFilter = {},
            currentFilter = PokemonFilter(),
            onRefresh = {},
            uiState = PokemonListUiState(
                pokemons = listOf(
                    Pokemon(
                        id = 1,
                        name = "Pikachu",
                        imageUrl = "https://example.com/pikachu.png",
                        types = listOf("Electric"),
                        hp = 35,
                        attack = 55,
                        defense = 40
                    ),
                    Pokemon(
                        id = 2,
                        name = "Charmander",
                        imageUrl = "https://example.com/charmander.png",
                        types = listOf("Fire"),
                        hp = 39,
                        attack = 52,
                        defense = 43
                    )
                ),
                isLoading = false,
                isEmpty = false,
                error = null,
                searchQuery = ""
            ),
            onApplyFilter = {},
            //onLoadNextItems = {},
            onClearFilter = {},
            onSearchQueryChange = {},
            pagedPokemons = null
        )
    }
}

@Preview
@Composable
private fun PokemonListScreenLoadingPreview() {
    MaterialTheme {
        PokemonListScreen(
            onNavigateToFilter = {},
            currentFilter = PokemonFilter(),
            onRefresh = {},
            uiState = PokemonListUiState(
                pokemons = emptyList(),
                isLoading = true,
                isEmpty = false,
                error = null,
                searchQuery = ""
            ),
            onApplyFilter = {},
            //onLoadNextItems = {},
            onClearFilter = {},
            onSearchQueryChange = {},
            pagedPokemons = null
        )
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
