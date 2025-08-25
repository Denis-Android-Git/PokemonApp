package com.example.pokemon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pokemon.domain.models.PokemonFilter
import com.example.pokemon.presentation.FilterScreen
import com.example.pokemon.presentation.PokemonListScreenState

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        var currentFilter by remember { mutableStateOf(PokemonFilter()) }

        NavHost(navController = navController, startDestination = "pokemon_list") {
            composable("pokemon_list") {
                PokemonListScreenState(
                    onNavigateToFilter = { navController.navigate("filter_screen") },
                    currentFilter = currentFilter,
                    onRefresh = {
                        currentFilter = PokemonFilter()
                        navController.navigate("pokemon_list")
                    }
                )
            }
            composable("filter_screen") {
                FilterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onApplyFilter = { filter ->
                        currentFilter = filter
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}