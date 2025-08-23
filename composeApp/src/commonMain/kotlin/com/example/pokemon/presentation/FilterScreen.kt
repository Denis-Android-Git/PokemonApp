package com.example.pokemon.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pokemon.domain.models.PokemonFilter
import com.example.pokemon.domain.models.PokemonSortOption
import com.example.pokemon.domain.models.PokemonFilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onNavigateBack: () -> Unit,
    onApplyFilter: (PokemonFilter) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(setOf<PokemonFilterType>()) }
    var selectedSortOption by remember { mutableStateOf(PokemonSortOption.NUMBER) }
    var sortAscending by remember { mutableStateOf(true) }
    var showSortOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтр и сортировка") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Сортировка",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Направление сортировки:")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { sortAscending = true },
                                    modifier = Modifier
                                        .background(
                                            if (sortAscending) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = "По возрастанию",
                                        tint = if (sortAscending) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { sortAscending = false },
                                    modifier = Modifier
                                        .background(
                                            if (!sortAscending) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "По убыванию",
                                        tint = if (!sortAscending) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = showSortOptions,
                            onExpandedChange = { showSortOptions = !showSortOptions }
                        ) {
                            OutlinedTextField(
                                value = selectedSortOption.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Сортировать по") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSortOptions) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showSortOptions,
                                onDismissRequest = { showSortOptions = false }
                            ) {
                                PokemonSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.displayName) },
                                        onClick = {
                                            selectedSortOption = option
                                            showSortOptions = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Фильтр по типам",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Выберите типы Pokémon для фильтрации:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyColumn(
                            modifier = Modifier.height(250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PokemonFilterType.entries.toTypedArray().chunked(4)) { rowTypes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTypes.forEach { type ->
                                        TypeChip(
                                            type = type,
                                            isSelected = selectedTypes.contains(type),
                                            onClick = {
                                                selectedTypes = if (selectedTypes.contains(type)) {
                                                    selectedTypes - type
                                                } else {
                                                    selectedTypes + type
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        val filter = PokemonFilter(
                            selectedTypes = selectedTypes,
                            sortOption = selectedSortOption,
                            sortAscending = sortAscending
                        )
                        onApplyFilter(filter)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                ) {
                    Text("Применить фильтр")
                }
            }
            
            item {
                OutlinedButton(
                    onClick = {
                        selectedTypes = emptySet()
                        selectedSortOption = PokemonSortOption.NUMBER
                        sortAscending = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить фильтры")
                }
            }
        }
    }
}

@Composable
private fun TypeChip(
    type: PokemonFilterType,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor = if (isSelected) {
        type.color.toColor()
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }


    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .height(40.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) type.color.toColor() else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun <T> Array<T>.chunked(size: Int): List<List<T>> {
    return this.toList().chunked(size)
}

fun String.toColor(): Color {
    val colorString = this.removePrefix("#")
    val colorLong = colorString.toLong(16)
    return when (colorString.length) {
        6 -> Color(colorLong or 0x00000000FF000000)
        8 -> Color(colorLong)
        else -> throw IllegalArgumentException("Invalid color format: $this")
    }
}


