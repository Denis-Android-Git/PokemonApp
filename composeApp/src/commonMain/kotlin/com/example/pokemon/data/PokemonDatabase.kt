package com.example.pokemon.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [PokemonEntity::class], version = 2)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<PokemonDatabase> {
    override fun initialize(): PokemonDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<PokemonDatabase>
): PokemonDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(
            dropAllTables = true
        )
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

