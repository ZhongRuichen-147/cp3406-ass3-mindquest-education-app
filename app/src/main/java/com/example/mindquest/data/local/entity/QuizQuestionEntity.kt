package com.example.mindquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached copy of a trivia question fetched from Open Trivia DB.
 * Caching (rather than re-fetching every session) means the Quiz activity keeps working
 * offline and the app doesn't need to talk to the network on every use.
 */
@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val difficulty: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswersJson: String,
    val fetchedAt: Long
)
