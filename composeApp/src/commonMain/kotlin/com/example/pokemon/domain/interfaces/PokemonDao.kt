package com.example.pokemon.domain.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokemon.data.PokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemons ORDER BY id ASC LIMIT :limit OFFSET :offset")
    fun getPokemonsWithPagination(limit: Int, offset: Int): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemons ORDER BY id ASC")
    fun getPokemonsWithoutPagination(): Flow<List<PokemonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemons: List<PokemonEntity>)

    @Query("SELECT * FROM pokemons WHERE LOWER(name) LIKE LOWER(:query) ORDER BY id ASC LIMIT :limit OFFSET :offset")
    fun searchPokemonsWithPagination(
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<PokemonEntity>>

    @Query("SELECT COUNT(*) FROM pokemons")
    suspend fun getPokemonCount(): Int

    @Query("SELECT COUNT(*) FROM pokemons WHERE LOWER(name) LIKE LOWER(:query)")
    suspend fun getPokemonCountWithSearch(query: String): Int

    @Query("DELETE FROM pokemons")
    suspend fun clearPokemons()
}
