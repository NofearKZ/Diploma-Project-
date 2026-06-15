package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val stars: Int = 0,
    val readTextsCount: Int = 0,
    val totalReadingTimeSeconds: Int = 0
)
