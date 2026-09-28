package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_history",
    indices = [Index(value = ["periodId"], unique = true)]
)
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodId: String,
    val number: Int,
    val size: String,
    val color: String,
    val createdAt: String,
    val timestamp: Long = System.currentTimeMillis()
)
