package com.example.mindquest.ui.activityscreen.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun QuizScreen(viewModel: QuizViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            uiState.errorMessage != null -> QuizErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::loadQuiz
            )

            uiState.isFinished -> QuizResultContent(
                score = uiState.score,
                total = uiState.questions.size,
                onPlayAgain = viewModel::restart
            )

            uiState.currentQuestion != null -> QuizQuestionContent(
                uiState = uiState,
                onSelectAnswer = viewModel::selectAnswer,
                onNext = viewModel::nextQuestion
            )
        }
    }
}

@Composable
private fun QuizErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Couldn't load questions", style = MaterialTheme.typography.titleLarge)
        Text(text = message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 12.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun QuizResultContent(score: Int, total: Int, onPlayAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Quiz complete! 🌟", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "You scored $score out of $total",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onPlayAgain, modifier = Modifier.testTag("quiz_play_again")) {
            Text("Play again")
        }
    }
}

@Composable
private fun QuizQuestionContent(
    uiState: QuizUiState,
    onSelectAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1f) / uiState.questions.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Question ${uiState.currentIndex + 1} of ${uiState.questions.size}",
            style = MaterialTheme.typography.bodyLarge
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(20.dp)
            )
        }

        question.options.forEach { option ->
            AnswerButton(
                text = option,
                state = when {
                    !uiState.isAnswered -> AnswerButtonState.NEUTRAL
                    option == question.correctAnswer -> AnswerButtonState.CORRECT
                    option == uiState.selectedAnswer -> AnswerButtonState.INCORRECT
                    else -> AnswerButtonState.NEUTRAL
                },
                enabled = !uiState.isAnswered,
                onClick = { onSelectAnswer(option) }
            )
        }

        if (uiState.isAnswered) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().testTag("quiz_next")
            ) {
                Text(if (uiState.isLastQuestion) "See results" else "Next question")
            }
        }
    }
}

private enum class AnswerButtonState { NEUTRAL, CORRECT, INCORRECT }

@Composable
private fun AnswerButton(
    text: String,
    state: AnswerButtonState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when (state) {
        AnswerButtonState.CORRECT -> Color(0xFF4CAF50)
        AnswerButtonState.INCORRECT -> Color(0xFFE53935)
        AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.secondaryContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("quiz_option"),
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon gives correct/incorrect a non-color cue too, for colorblind users.
            when (state) {
                AnswerButtonState.CORRECT -> Icon(Icons.Filled.Check, contentDescription = null)
                AnswerButtonState.INCORRECT -> Icon(Icons.Filled.Close, contentDescription = null)
                AnswerButtonState.NEUTRAL -> Unit
            }
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
