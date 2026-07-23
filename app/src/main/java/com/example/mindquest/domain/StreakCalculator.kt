package com.example.mindquest.domain

import java.util.TimeZone

private const val DAY_MS = 24L * 60 * 60 * 1000

private fun dayBucket(timestampMs: Long): Long {
    val offsetMs = TimeZone.getDefault().getOffset(timestampMs)
    return (timestampMs + offsetMs) / DAY_MS
}

fun isSameLocalDay(a: Long, b: Long): Boolean = dayBucket(a) == dayBucket(b)

/**
 * Consecutive-day streak, counting back from today. A streak survives if the most recent
 * play was today OR yesterday (still "alive" until today ends); anything older breaks it.
 * Pure function over epoch-millis timestamps — no Android framework dependency, easy to unit test.
 */
fun calculateStreakDays(timestamps: List<Long>, now: Long = System.currentTimeMillis()): Int {
    if (timestamps.isEmpty()) return 0

    val distinctDaysDesc = timestamps.map(::dayBucket).toSortedSet().toList().sortedDescending()
    val today = dayBucket(now)
    val mostRecent = distinctDaysDesc.first()

    if (mostRecent < today - 1) return 0

    var cursor = if (mostRecent == today) today else today - 1
    var streak = 0
    for (day in distinctDaysDesc) {
        when {
            day == cursor -> {
                streak++
                cursor--
            }
            day < cursor -> break
        }
    }
    return streak
}
