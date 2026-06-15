package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val textId: String,
    val durationSeconds: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    val mistakes: Int = 0
)
