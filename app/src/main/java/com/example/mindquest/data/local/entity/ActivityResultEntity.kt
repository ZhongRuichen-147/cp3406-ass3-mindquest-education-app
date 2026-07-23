package com.example.mindquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActivityType { QUIZ, MEMORY }

/**
 * A single completed play session, either a Quiz round or a Memory Match round.
 * This is the only per-user data MindQuest stores — no account, no PII, no location.
 */
@Entity(tableName = "activity_results")
data class ActivityResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ActivityType,
    val timestamp: Long,
    val score: Int,
    val correctCount: Int,
    val totalCount: Int,
    val durationMs: Long
)
