package com.example.pokemon.domain.models

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val types: List<String> = emptyList()
)
