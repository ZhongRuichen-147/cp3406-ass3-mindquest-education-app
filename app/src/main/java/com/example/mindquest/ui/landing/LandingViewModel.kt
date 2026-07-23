package com.example.mindquest.ui.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.domain.calculateStreakDays
import com.example.mindquest.domain.isSameLocalDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LandingUiState(
    val gamesPlayed: Int = 0,
    val streakDays: Int = 0,
    val hasPlayedToday: Boolean = false
)

class LandingViewModel(statsRepository: StatsRepository) : ViewModel() {

    val uiState: StateFlow<LandingUiState> = statsRepository.observeRecentResults(limit = 200)
        .map { results ->
            val timestamps = results.map { it.timestamp }
            val now = System.currentTimeMillis()
            LandingUiState(
                gamesPlayed = results.size,
                streakDays = calculateStreakDays(timestamps, now),
                hasPlayedToday = timestamps.any { isSameLocalDay(it, now) }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LandingUiState())
}
