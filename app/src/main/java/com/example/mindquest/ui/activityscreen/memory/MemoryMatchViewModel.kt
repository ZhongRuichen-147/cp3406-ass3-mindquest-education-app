package com.example.mindquest.ui.activityscreen.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.domain.ActivityResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class MemoryUiState(
    val cards: List<MemoryCard> = emptyList(),
    val moves: Int = 0,
    val matchedPairs: Int = 0,
    val totalPairs: Int = 0,
    val isFinished: Boolean = false,
    val isBusy: Boolean = false
)

val MEMORY_SYMBOL_POOL = listOf("🐶", "🐱", "🐰", "🦊", "🐻", "🐼", "🐸", "🦁", "🐵", "🐨")

private const val MISMATCH_DELAY_MS = 700L

class MemoryMatchViewModel(
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    private var firstFlippedIndex: Int? = null
    private var startTime = System.currentTimeMillis()

    init {
        startNewGame()
    }

    fun startNewGame() {
        viewModelScope.launch {
            val pairs = settingsRepository.settings.first().difficulty.memoryPairs
            val symbols = MEMORY_SYMBOL_POOL.take(pairs)
            val deck = (symbols + symbols)
                .shuffled()
                .mapIndexed { index, symbol -> MemoryCard(id = index, symbol = symbol) }

            firstFlippedIndex = null
            startTime = System.currentTimeMillis()
            _uiState.value = MemoryUiState(cards = deck, totalPairs = pairs)
        }
    }

    fun onCardClick(index: Int) {
        val state = _uiState.value
        if (state.isBusy || state.isFinished) return
        val card = state.cards.getOrNull(index) ?: return
        if (card.isFlipped || card.isMatched) return

        val revealed = flip(state.cards, index)
        val first = firstFlippedIndex

        if (first == null) {
            firstFlippedIndex = index
            _uiState.value = state.copy(cards = revealed)
            return
        }

        firstFlippedIndex = null
        _uiState.value = state.copy(cards = revealed, moves = state.moves + 1, isBusy = true)

        viewModelScope.launch {
            if (revealed[first].symbol == revealed[index].symbol) {
                val matched = revealed.mapIndexed { i, c ->
                    if (i == first || i == index) c.copy(isMatched = true) else c
                }
                val matchedPairs = state.matchedPairs + 1
                val isFinished = matchedPairs == state.totalPairs
                _uiState.value = _uiState.value.copy(
                    cards = matched,
                    matchedPairs = matchedPairs,
                    isBusy = false,
                    isFinished = isFinished
                )
                if (isFinished) recordCompletion(matchedPairs)
            } else {
                delay(MISMATCH_DELAY_MS)
                val hidden = revealed.mapIndexed { i, c ->
                    if (i == first || i == index) c.copy(isFlipped = false) else c
                }
                _uiState.value = _uiState.value.copy(cards = hidden, isBusy = false)
            }
        }
    }

    private fun flip(cards: List<MemoryCard>, index: Int): List<MemoryCard> =
        cards.mapIndexed { i, c -> if (i == index) c.copy(isFlipped = true) else c }

    private suspend fun recordCompletion(totalPairs: Int) {
        statsRepository.recordResult(
            ActivityResult(
                type = ActivityType.MEMORY,
                timestamp = System.currentTimeMillis(),
                score = totalPairs,
                correctCount = totalPairs,
                totalCount = totalPairs,
                durationMs = System.currentTimeMillis() - startTime
            )
        )
    }
}
