package com.example.mindquest.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.domain.ActivityResult
import com.example.mindquest.domain.calculateStreakDays
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val totalGames: Int = 0,
    val quizGames: Int = 0,
    val memoryGames: Int = 0,
    val averageAccuracyPercent: Int = 0,
    val bestQuizScore: Int = 0,
    val streakDays: Int = 0,
    val recent: List<ActivityResult> = emptyList()
)

class StatisticsViewModel(statsRepository: StatsRepository) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = statsRepository.observeRecentResults(limit = 200)
        .map(::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())
}

internal fun toUiState(results: List<ActivityResult>): StatisticsUiState {
    val quiz = results.filter { it.type == ActivityType.QUIZ }
    val memory = results.filter { it.type == ActivityType.MEMORY }
    val totalAnswered = quiz.sumOf { it.totalCount }
    val accuracy = if (totalAnswered > 0) (quiz.sumOf { it.correctCount } * 100) / totalAnswered else 0

    return StatisticsUiState(
        totalGames = results.size,
        quizGames = quiz.size,
        memoryGames = memory.size,
        averageAccuracyPercent = accuracy,
        bestQuizScore = quiz.maxOfOrNull { it.score } ?: 0,
        streakDays = calculateStreakDays(results.map { it.timestamp }),
        recent = results.take(10)
    )
}
