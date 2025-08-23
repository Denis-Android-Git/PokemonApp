package com.example.pokemon.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemons")
data class PokemonEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val types: String = ""
)



