package com.example.data

import com.example.data.local.ReadingDao
import com.example.data.local.ReadingSession
import com.example.data.local.UserStats
import kotlinx.coroutines.flow.Flow

class ReadingRepository(private val dao: ReadingDao) {
    val userStats: Flow<UserStats?> = dao.getUserStats()
    val allSessions: Flow<List<ReadingSession>> = dao.getAllSessions()

    suspend fun saveSession(session: ReadingSession) {
        dao.insertSession(session)
    }

    suspend fun updateStats(stats: UserStats) {
        dao.updateUserStats(stats)
    }
}
