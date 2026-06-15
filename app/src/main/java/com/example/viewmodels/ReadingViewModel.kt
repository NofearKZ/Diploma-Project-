package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ReadingRepository
import com.example.data.local.ReadingSession
import com.example.data.local.UserStats
import com.example.data.models.TextItem
import com.example.data.models.sampleTexts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadingViewModel(private val repository: ReadingRepository) : ViewModel() {

    val userStats: StateFlow<UserStats> = repository.userStats
        .map { it ?: UserStats() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    val sessions: StateFlow<List<ReadingSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentLanguage = MutableStateFlow("ru")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _availableTexts = MutableStateFlow(sampleTexts)
    val availableTexts: StateFlow<List<TextItem>> = _availableTexts.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun finishReading(textItem: TextItem, durationSecs: Int, mistakes: Int, starsEarned: Int) {
        viewModelScope.launch {
            val session = ReadingSession(
                textId = textItem.id,
                durationSeconds = durationSecs,
                mistakes = mistakes
            )
            repository.saveSession(session)

            val currentStats = userStats.value
            repository.updateStats(
                currentStats.copy(
                    stars = currentStats.stars + starsEarned,
                    readTextsCount = currentStats.readTextsCount + 1,
                    totalReadingTimeSeconds = currentStats.totalReadingTimeSeconds + durationSecs
                )
            )
        }
    }
}

class ReadingViewModelFactory(private val repository: ReadingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReadingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
