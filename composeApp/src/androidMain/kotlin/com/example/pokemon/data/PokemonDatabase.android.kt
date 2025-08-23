package com.example.pokemon.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<PokemonDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("weather.db")
    return Room.databaseBuilder<PokemonDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}