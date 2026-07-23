package com.example.mindquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mindquest.data.local.entity.QuizQuestionEntity

@Dao
interface QuizQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM quiz_questions WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(difficulty: String, limit: Int): List<QuizQuestionEntity>

    @Query("SELECT COUNT(*) FROM quiz_questions WHERE difficulty = :difficulty")
    suspend fun countByDifficulty(difficulty: String): Int

    @Query("DELETE FROM quiz_questions")
    suspend fun clearAll()
}
