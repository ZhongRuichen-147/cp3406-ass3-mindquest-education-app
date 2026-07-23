package com.example.mindquest.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    private val dayMs = 24L * 60 * 60 * 1000
    private val now = 1_700_000_000_000L

    @Test
    fun `no activity means zero streak`() {
        assertEquals(0, calculateStreakDays(emptyList(), now))
    }

    @Test
    fun `three consecutive days including today gives a streak of three`() {
        val timestamps = listOf(now, now - dayMs, now - 2 * dayMs)
        assertEquals(3, calculateStreakDays(timestamps, now))
    }

    @Test
    fun `a play yesterday but not today keeps the streak alive`() {
        val timestamps = listOf(now - dayMs, now - 2 * dayMs)
        assertEquals(2, calculateStreakDays(timestamps, now))
    }

    @Test
    fun `a gap of more than one day resets the streak`() {
        val timestamps = listOf(now - 3 * dayMs)
        assertEquals(0, calculateStreakDays(timestamps, now))
    }

    @Test
    fun `multiple plays on the same day only count once`() {
        val timestamps = listOf(now, now - 1000, now - 2000)
        assertEquals(1, calculateStreakDays(timestamps, now))
    }
}
