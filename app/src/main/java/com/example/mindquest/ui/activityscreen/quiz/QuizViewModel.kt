package com.example.mindquest.ui.activityscreen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindquest.data.NetworkResult
import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.data.repository.QuizRepository
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.domain.ActivityResult
import com.example.mindquest.domain.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val score: Int = 0,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
) {
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)
    val isAnswered: Boolean get() = selectedAnswer != null
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
}

const val QUIZ_QUESTION_COUNT = 5

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var startTime = System.currentTimeMillis()

    init {
        loadQuiz()
    }

    fun loadQuiz() {
        startTime = System.currentTimeMillis()
        viewModelScope.launch {
            _uiState.value = QuizUiState(isLoading = true)
            val difficulty = settingsRepository.settings.first().difficulty
            when (val result = quizRepository.getQuizQuestions(difficulty, QUIZ_QUESTION_COUNT)) {
                is NetworkResult.Success ->
                    _uiState.value = QuizUiState(isLoading = false, questions = result.data)
                is NetworkResult.Error ->
                    _uiState.value = QuizUiState(isLoading = false, errorMessage = result.error)
            }
        }
    }

    fun selectAnswer(answer: String) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.isAnswered) return

        val isCorrect = answer == question.correctAnswer
        _uiState.value = state.copy(
            selectedAnswer = answer,
            score = if (isCorrect) state.score + 1 else state.score
        )
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.isAnswered) return

        if (state.isLastQuestion) {
            finishQuiz(state)
        } else {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1, selectedAnswer = null)
        }
    }

    private fun finishQuiz(state: QuizUiState) {
        _uiState.value = state.copy(isFinished = true)
        viewModelScope.launch {
            statsRepository.recordResult(
                ActivityResult(
                    type = ActivityType.QUIZ,
                    timestamp = System.currentTimeMillis(),
                    score = state.score,
                    correctCount = state.score,
                    totalCount = state.questions.size,
                    durationMs = System.currentTimeMillis() - startTime
                )
            )
        }
    }

    fun restart() = loadQuiz()
}
