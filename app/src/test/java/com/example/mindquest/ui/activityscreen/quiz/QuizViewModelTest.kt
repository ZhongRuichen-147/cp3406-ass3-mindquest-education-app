package com.example.mindquest.ui.activityscreen.quiz

import com.example.mindquest.data.NetworkResult
import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.data.repository.QuizRepository
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.settings.Difficulty
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.data.settings.UserSettings
import com.example.mindquest.domain.ActivityResult
import com.example.mindquest.domain.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val questions = listOf(
        QuizQuestion("2+2?", "4", listOf("3", "4", "5")),
        QuizQuestion("Sky color?", "Blue", listOf("Blue", "Green", "Red"))
    )

    private val fakeQuizRepository = object : QuizRepository {
        override suspend fun getQuizQuestions(difficulty: Difficulty, count: Int): NetworkResult<List<QuizQuestion>> =
            NetworkResult.Success(questions)
        override suspend fun clearCache() {}
    }

    private val recorded = mutableListOf<ActivityResult>()
    private val fakeStatsRepository = object : StatsRepository {
        override fun observeAllResults() = MutableStateFlow(emptyList<ActivityResult>())
        override fun observeRecentResults(limit: Int) = MutableStateFlow(emptyList<ActivityResult>())
        override suspend fun recordResult(result: ActivityResult) {
            recorded.add(result)
        }
        override suspend fun clearHistory() {}
    }

    private val fakeSettingsRepository = object : SettingsRepository {
        override val settings = MutableStateFlow(UserSettings())
        override suspend fun setDifficulty(difficulty: Difficulty) {}
        override suspend fun setSoundEnabled(enabled: Boolean) {}
        override suspend fun setDarkTheme(enabled: Boolean) {}
        override suspend fun clearAll() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): QuizViewModel {
        val viewModel = QuizViewModel(fakeQuizRepository, fakeStatsRepository, fakeSettingsRepository)
        dispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `loading a quiz populates the first question`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("2+2?", state.currentQuestion?.question)
    }

    @Test
    fun `selecting the correct answer increments the score`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.selectAnswer("4")

        assertEquals(1, viewModel.uiState.value.score)
        assertEquals("4", viewModel.uiState.value.selectedAnswer)
    }

    @Test
    fun `selecting an incorrect answer does not increment the score`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.selectAnswer("3")

        assertEquals(0, viewModel.uiState.value.score)
    }

    @Test
    fun `answering the same question twice is ignored`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.selectAnswer("4")
        viewModel.selectAnswer("3")

        assertEquals("4", viewModel.uiState.value.selectedAnswer)
        assertEquals(1, viewModel.uiState.value.score)
    }

    @Test
    fun `finishing the last question records a result and marks the quiz finished`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.selectAnswer("4")
        viewModel.nextQuestion()
        viewModel.selectAnswer("Blue")
        viewModel.nextQuestion()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(2, viewModel.uiState.value.score)
        assertEquals(1, recorded.size)
        assertEquals(ActivityType.QUIZ, recorded.first().type)
    }
}
