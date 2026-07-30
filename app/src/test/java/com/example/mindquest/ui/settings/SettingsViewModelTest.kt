package com.example.mindquest.ui.settings

import com.example.mindquest.data.NetworkResult
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private var settingsCleared = false
    private val fakeSettingsRepository = object : SettingsRepository {
        override val settings = MutableStateFlow(UserSettings())
        override suspend fun setDifficulty(difficulty: Difficulty) {}
        override suspend fun setSoundEnabled(enabled: Boolean) {}
        override suspend fun setDarkTheme(enabled: Boolean) {}
        override suspend fun clearAll() {
            settingsCleared = true
        }
    }

    private var cacheCleared = false
    private val fakeQuizRepository = object : QuizRepository {
        override suspend fun getQuizQuestions(difficulty: Difficulty, count: Int): NetworkResult<List<QuizQuestion>> =
            NetworkResult.Success(emptyList())
        override suspend fun clearCache() {
            cacheCleared = true
        }
    }

    private var historyCleared = false
    private val fakeStatsRepository = object : StatsRepository {
        override fun observeAllResults() = MutableStateFlow(emptyList<ActivityResult>())
        override fun observeRecentResults(limit: Int) = MutableStateFlow(emptyList<ActivityResult>())
        override suspend fun recordResult(result: ActivityResult) {}
        override suspend fun clearHistory() {
            historyCleared = true
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SettingsViewModel(fakeSettingsRepository, fakeQuizRepository, fakeStatsRepository)

    @Test
    fun `an incorrect parental gate answer blocks the data clear`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.requestClearData()
        val state = viewModel.uiState.value
        val wrongAnswer = state.gateA + state.gateB + 1

        viewModel.updateGateAnswer(wrongAnswer.toString())
        viewModel.submitGateAnswer()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.gateError)
        assertFalse(settingsCleared)
        assertFalse(cacheCleared)
        assertFalse(historyCleared)
    }

    @Test
    fun `a correct parental gate answer clears all three data sources`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.requestClearData()
        val state = viewModel.uiState.value

        viewModel.updateGateAnswer((state.gateA + state.gateB).toString())
        viewModel.submitGateAnswer()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(settingsCleared)
        assertTrue(cacheCleared)
        assertTrue(historyCleared)
        assertFalse(viewModel.uiState.value.isParentGateVisible)
        assertTrue(viewModel.uiState.value.dataClearedMessage)
    }
}
