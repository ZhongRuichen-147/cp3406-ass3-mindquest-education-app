package com.example.mindquest.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TriviaResponseDto(
    val response_code: Int,
    val results: List<TriviaQuestionDto>
)

@Serializable
data class TriviaQuestionDto(
    val category: String,
    val difficulty: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)
