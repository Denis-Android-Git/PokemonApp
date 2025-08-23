package com.example.pokemon.domain.models

enum class PokemonFilterType(val displayName: String, val color: String) {
    NORMAL("Normal", "#A8A878"),
    FIRE("Fire", "#F08030"),
    WATER("Water", "#6890F0"),
    ELECTRIC("Electric", "#F8D030"),
    GRASS("Grass", "#78C850"),
    ICE("Ice", "#98D8D8"),
    FIGHTING("Fighting", "#C03028"),
    POISON("Poison", "#A040A0"),
    GROUND("Ground", "#E0C068"),
    FLYING("Flying", "#A890F0"),
    PSYCHIC("Psychic", "#F85888"),
    BUG("Bug", "#A8B820"),
    ROCK("Rock", "#B8A038"),
    GHOST("Ghost", "#705898"),
    DRAGON("Dragon", "#7038F8"),
    DARK("Dark", "#705848"),
    STEEL("Steel", "#B8B8D0"),
    FAIRY("Fairy", "#EE99AC")
}