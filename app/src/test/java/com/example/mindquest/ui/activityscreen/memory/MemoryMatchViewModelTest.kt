package com.example.mindquest.ui.activityscreen.memory

import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.settings.Difficulty
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.data.settings.UserSettings
import com.example.mindquest.domain.ActivityResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryMatchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val recorded = mutableListOf<ActivityResult>()
    private val fakeStatsRepository = object : StatsRepository {
        override fun observeRecentResults(limit: Int) = MutableStateFlow(emptyList<ActivityResult>())
        override suspend fun recordResult(result: ActivityResult) {
            recorded.add(result)
        }
        override suspend fun clearHistory() {}
    }

    private val fakeSettingsRepository = object : SettingsRepository {
        override val settings = MutableStateFlow(UserSettings(difficulty = Difficulty.EASY))
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

    private fun createViewModel(): MemoryMatchViewModel {
        val viewModel = MemoryMatchViewModel(fakeStatsRepository, fakeSettingsRepository)
        dispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `deck size matches the easy difficulty pair count`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        assertEquals(6, viewModel.uiState.value.totalPairs)
        assertEquals(12, viewModel.uiState.value.cards.size)
    }

    @Test
    fun `clicking a matching pair marks both cards matched and increments moves`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val cards = viewModel.uiState.value.cards
        val first = cards.first()
        val secondIndex = cards.indexOfFirst { it.id != first.id && it.symbol == first.symbol }

        viewModel.onCardClick(cards.indexOf(first))
        viewModel.onCardClick(secondIndex)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.moves)
        assertEquals(1, state.matchedPairs)
        assertTrue(state.cards[cards.indexOf(first)].isMatched)
        assertTrue(state.cards[secondIndex].isMatched)
    }

    @Test
    fun `clicking a mismatched pair flips both cards back down`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val cards = viewModel.uiState.value.cards
        val first = cards.first()
        val mismatchIndex = cards.indexOfFirst { it.symbol != first.symbol }

        viewModel.onCardClick(cards.indexOf(first))
        viewModel.onCardClick(mismatchIndex)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.moves)
        assertEquals(0, state.matchedPairs)
        assertFalse(state.cards[cards.indexOf(first)].isFlipped)
        assertFalse(state.cards[mismatchIndex].isFlipped)
    }

    @Test
    fun `matching every pair records a completed activity result`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        while (viewModel.uiState.value.matchedPairs < viewModel.uiState.value.totalPairs) {
            val cards = viewModel.uiState.value.cards
            val first = cards.first { !it.isMatched }
            val secondIndex = cards.indexOfFirst {
                it.id != first.id && it.symbol == first.symbol && !it.isMatched
            }
            viewModel.onCardClick(cards.indexOf(first))
            viewModel.onCardClick(secondIndex)
            dispatcher.scheduler.advanceUntilIdle()
        }

        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(1, recorded.size)
    }
}
