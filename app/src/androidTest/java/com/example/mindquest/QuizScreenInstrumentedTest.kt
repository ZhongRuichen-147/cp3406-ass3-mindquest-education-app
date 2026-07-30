package com.example.mindquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.mindquest.data.NetworkResult
import com.example.mindquest.data.repository.QuizRepository
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.settings.Difficulty
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.data.settings.UserSettings
import com.example.mindquest.domain.ActivityResult
import com.example.mindquest.domain.QuizQuestion
import com.example.mindquest.ui.activityscreen.quiz.QuizScreen
import com.example.mindquest.ui.activityscreen.quiz.QuizViewModel
import com.example.mindquest.ui.theme.MindQuestTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Drives QuizScreen with hand-built fake repositories instead of the real Koin graph,
 * so this test is deterministic and doesn't depend on a network connection or a real device DB.
 */
class QuizScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeQuizRepository = object : QuizRepository {
        override suspend fun getQuizQuestions(difficulty: Difficulty, count: Int): NetworkResult<List<QuizQuestion>> =
            NetworkResult.Success(listOf(QuizQuestion("What is 2 + 2?", "4", listOf("3", "4", "5"))))
        override suspend fun clearCache() {}
    }

    private val fakeStatsRepository = object : StatsRepository {
        override fun observeRecentResults(limit: Int) = MutableStateFlow(emptyList<ActivityResult>())
        override suspend fun recordResult(result: ActivityResult) {}
        override suspend fun clearHistory() {}
    }

    private val fakeSettingsRepository = object : SettingsRepository {
        override val settings = MutableStateFlow(UserSettings())
        override suspend fun setDifficulty(difficulty: Difficulty) {}
        override suspend fun setSoundEnabled(enabled: Boolean) {}
        override suspend fun setDarkTheme(enabled: Boolean) {}
        override suspend fun clearAll() {}
    }

    // Constructing the ViewModel directly (instead of via a factory) is intentional here —
    // it's how a fake-repo-backed ViewModel gets injected for a deterministic UI test.
    @Suppress("ViewModelConstructorInComposable")
    @Test
    fun selectingAnAnswerRevealsTheNextQuestionButton() {
        composeTestRule.setContent {
            MindQuestTheme {
                QuizScreen(
                    viewModel = QuizViewModel(fakeQuizRepository, fakeStatsRepository, fakeSettingsRepository)
                )
            }
        }

        composeTestRule.onNodeWithText("What is 2 + 2?").assertIsDisplayed()
        composeTestRule.onNodeWithText("4").performClick()
        composeTestRule.onNodeWithTag("quiz_next").assertIsDisplayed()
    }
}
