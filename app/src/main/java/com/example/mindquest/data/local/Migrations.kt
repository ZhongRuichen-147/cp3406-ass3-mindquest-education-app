package com.example.mindquest.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds a unique (question, difficulty) index to quiz_questions so repeated syncs stop
 * accumulating duplicate cached questions. Any duplicates already on a version-1 database must
 * be removed first — SQLite refuses to build a unique index over data that violates it.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM quiz_questions WHERE id NOT IN (
                SELECT MIN(id) FROM quiz_questions GROUP BY question, difficulty
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_quiz_questions_question_difficulty` " +
                "ON `quiz_questions` (`question`, `difficulty`)"
        )
    }
}
