package com.example.mindquest.data.repository

import com.example.mindquest.data.local.dao.ActivityResultDao
import com.example.mindquest.data.local.entity.ActivityResultEntity
import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.domain.ActivityResult
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val dao = mockk<ActivityResultDao>(relaxUnitFun = true)
    private val repository = StatsRepositoryImpl(dao, dispatcher)

    @Test
    fun `observeRecentResults maps entities to domain models`() = runTest(dispatcher) {
        val entity = ActivityResultEntity(
            id = 1,
            type = ActivityType.QUIZ,
            timestamp = 1000L,
            score = 4,
            correctCount = 4,
            totalCount = 5,
            durationMs = 12000L
        )
        every { dao.observeRecent(50) } returns flowOf(listOf(entity))

        val results = repository.observeRecentResults().first()

        assertEquals(1, results.size)
        assertEquals(4, results.first().score)
        assertEquals(ActivityType.QUIZ, results.first().type)
    }

    @Test
    fun `recordResult inserts a mapped entity`() = runTest(dispatcher) {
        val result = ActivityResult(
            type = ActivityType.MEMORY,
            timestamp = 500L,
            score = 6,
            correctCount = 6,
            totalCount = 6,
            durationMs = 8000L
        )

        repository.recordResult(result)

        coVerify { dao.insert(match { it.type == ActivityType.MEMORY && it.score == 6 }) }
    }

    @Test
    fun `clearHistory delegates to the dao`() = runTest(dispatcher) {
        repository.clearHistory()

        coVerify { dao.clearAll() }
    }
}
