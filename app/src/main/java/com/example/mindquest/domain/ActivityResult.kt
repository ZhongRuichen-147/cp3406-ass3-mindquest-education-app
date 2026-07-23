package com.example.mindquest.domain

import com.example.mindquest.data.local.entity.ActivityType

data class ActivityResult(
    val type: ActivityType,
    val timestamp: Long,
    val score: Int,
    val correctCount: Int,
    val totalCount: Int,
    val durationMs: Long
)
