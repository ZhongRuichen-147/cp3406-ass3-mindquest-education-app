package com.example.mindquest.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached copy of a trivia question fetched from Open Trivia DB.
 * Caching (rather than re-fetching every session) means the Quiz activity keeps working
 * offline and the app doesn't need to talk to the network on every use.
 *
 * The (question, difficulty) unique index lets repeated syncs skip questions already cached
 * instead of accumulating duplicate rows every time a quiz starts.
 */
@Entity(
    tableName = "quiz_questions",
    indices = [Index(value = ["question", "difficulty"], unique = true)]
)
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val difficulty: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswersJson: String,
    val fetchedAt: Long
)
