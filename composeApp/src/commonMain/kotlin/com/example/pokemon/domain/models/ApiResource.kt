package com.example.pokemon.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)

