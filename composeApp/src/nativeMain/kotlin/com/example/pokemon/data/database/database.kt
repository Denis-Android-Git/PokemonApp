package com.example.pokemon.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pokemon.data.PokemonDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask


fun getDatabaseBuilder(): RoomDatabase.Builder<PokemonDatabase> {
    val paths = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    )
    val documentsDirectory = paths.firstOrNull() as? String
        ?: throw IllegalStateException("Unable to find documents directory")

    val dbFile = "$documentsDirectory/weather.db"

    return Room.databaseBuilder<PokemonDatabase>(
        name = dbFile
    )
}