package com.example.mindquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mindquest.data.local.dao.ActivityResultDao
import com.example.mindquest.data.local.dao.QuizQuestionDao
import com.example.mindquest.data.local.entity.ActivityResultEntity
import com.example.mindquest.data.local.entity.QuizQuestionEntity

@Database(
    entities = [QuizQuestionEntity::class, ActivityResultEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MindQuestDatabase : RoomDatabase() {
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun activityResultDao(): ActivityResultDao
}
