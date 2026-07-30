package com.example.mindquest.ui.stats

import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.domain.ActivityResult
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsUiStateMapperTest {

    @Test
    fun `accuracy is computed from correct over total answered across all quiz sessions`() {
        val results = listOf(
            ActivityResult(ActivityType.QUIZ, 1000L, score = 4, correctCount = 4, totalCount = 5, durationMs = 1000L),
            ActivityResult(ActivityType.QUIZ, 2000L, score = 3, correctCount = 3, totalCount = 5, durationMs = 1000L),
            ActivityResult(ActivityType.MEMORY, 3000L, score = 6, correctCount = 6, totalCount = 6, durationMs = 1000L)
        )

        val state = toUiState(results)

        assertEquals(3, state.totalGames)
        assertEquals(2, state.quizGames)
        assertEquals(1, state.memoryGames)
        assertEquals(70, state.averageAccuracyPercent)
        assertEquals(4, state.bestQuizScore)
    }

    @Test
    fun `totals are not capped once history exceeds a couple hundred sessions`() {
        // Regression test: totalGames/bestQuizScore/streak must come from the full history,
        // not a capped "recent" query, or these stats silently stop growing past the cap.
        val oldBestScore = ActivityResult(
            ActivityType.QUIZ, timestamp = 1L, score = 99, correctCount = 5, totalCount = 5, durationMs = 1000L
        )
        val fillerResults = (1..250).map { i ->
            ActivityResult(ActivityType.QUIZ, timestamp = i.toLong() + 1, score = 1, correctCount = 1, totalCount = 5, durationMs = 1000L)
        }

        val state = toUiState(listOf(oldBestScore) + fillerResults)

        assertEquals(251, state.totalGames)
        assertEquals(99, state.bestQuizScore)
    }

    @Test
    fun `no quiz games means zero accuracy instead of a division error`() {
        val results = listOf(
            ActivityResult(ActivityType.MEMORY, 1000L, score = 6, correctCount = 6, totalCount = 6, durationMs = 1000L)
        )

        val state = toUiState(results)

        assertEquals(0, state.averageAccuracyPercent)
        assertEquals(0, state.bestQuizScore)
    }
}
