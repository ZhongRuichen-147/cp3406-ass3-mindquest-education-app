package com.example.mindquest.domain

data class QuizQuestion(
    val question: String,
    val correctAnswer: String,
    val options: List<String>
)
