package com.example.pokemon.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val base_experience: Int?,
    val height: Int,
    val is_default: Boolean,
    val order: Int,
    val weight: Int,
    val abilities: List<PokemonAbility>,
    val forms: List<NamedApiResource>,
    val game_indices: List<VersionGameIndex>,
    val held_items: List<PokemonHeldItem>,
    val location_area_encounters: String,
    val moves: List<PokemonMove>,
    val past_types: List<PokemonTypePast>,
    val past_abilities: List<PokemonAbilityPast>,
    val sprites: PokemonSprites,
    val cries: PokemonCries?,
    val species: NamedApiResource,
    val stats: List<PokemonStat>,
    val types: List<PokemonType>
)

@Serializable
data class PokemonAbility(
    val is_hidden: Boolean,
    val slot: Int,
    val ability: NamedApiResource
)

@Serializable
data class PokemonType(
    val slot: Int,
    val type: NamedApiResource
)

@Serializable
data class PokemonTypePast(
    val generation: NamedApiResource,
    val types: List<PokemonType>
)

@Serializable
data class PokemonAbilityPast(
    val generation: NamedApiResource,
    val abilities: List<PokemonAbility?> = emptyList()
)

@Serializable
data class PokemonHeldItem(
    val item: NamedApiResource,
    val version_details: List<PokemonHeldItemVersion>
)

@Serializable
data class PokemonHeldItemVersion(
    val version: NamedApiResource,
    val rarity: Int
)

@Serializable
data class PokemonMove(
    val move: NamedApiResource,
    val version_group_details: List<PokemonMoveVersion>
)

@Serializable
data class PokemonMoveVersion(
    val move_learn_method: NamedApiResource,
    val version_group: NamedApiResource,
    val level_learned_at: Int,
    val order: Int?
)

@Serializable
data class PokemonStat(
    val stat: NamedApiResource,
    val effort: Int,
    val base_stat: Int
)

@Serializable
data class PokemonSprites(
    val front_default: String?,
    val front_shiny: String?,
    val front_female: String?,
    val front_shiny_female: String?,
    val back_default: String?,
    val back_shiny: String?,
    val back_female: String?,
    val back_shiny_female: String?
)

@Serializable
data class PokemonCries(
    val latest: String?,
    val legacy: String?
)

@Serializable
data class VersionGameIndex(
    val game_index: Int,
    val version: NamedApiResource
)
