package com.example.mindquest.data.settings

/** One setting drives both the Quiz difficulty (Open Trivia DB) and the Memory Match grid size. */
enum class Difficulty(val label: String, val triviaValue: String, val memoryPairs: Int) {
    EASY("Easy", "easy", 6),
    MEDIUM("Medium", "medium", 8),
    HARD("Hard", "hard", 10)
}

data class UserSettings(
    val difficulty: Difficulty = Difficulty.EASY,
    val soundEnabled: Boolean = true,
    val darkTheme: Boolean = false
)
