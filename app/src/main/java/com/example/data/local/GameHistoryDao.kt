package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {

    @Query("SELECT * FROM game_history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history ORDER BY id DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentHistoryList(limit: Int): List<GameHistoryEntity>

    @Query("SELECT number FROM game_history ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentNumbers(limit: Int): List<Int>

    @Query("SELECT COUNT(*) FROM game_history")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: GameHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<GameHistoryEntity>): List<Long>

    @Query("DELETE FROM game_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM game_history")
    suspend fun clearAll()
}
