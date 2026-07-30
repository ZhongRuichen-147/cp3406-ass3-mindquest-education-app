package com.example.mindquest.data.repository

import com.example.mindquest.data.local.dao.ActivityResultDao
import com.example.mindquest.data.local.entity.ActivityResultEntity
import com.example.mindquest.domain.ActivityResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface StatsRepository {
    /** Full play history, unbounded — use for aggregate stats (totals, best score, streak). */
    fun observeAllResults(): Flow<List<ActivityResult>>

    /** Bounded to [limit] — use only for a "recent activity" display list, never for aggregates. */
    fun observeRecentResults(limit: Int = 10): Flow<List<ActivityResult>>
    suspend fun recordResult(result: ActivityResult)
    suspend fun clearHistory()
}

class StatsRepositoryImpl(
    private val dao: ActivityResultDao,
    private val dispatcher: CoroutineDispatcher
) : StatsRepository {

    override fun observeAllResults(): Flow<List<ActivityResult>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRecentResults(limit: Int): Flow<List<ActivityResult>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun recordResult(result: ActivityResult) {
        withContext(dispatcher) {
            dao.insert(
                ActivityResultEntity(
                    type = result.type,
                    timestamp = result.timestamp,
                    score = result.score,
                    correctCount = result.correctCount,
                    totalCount = result.totalCount,
                    durationMs = result.durationMs
                )
            )
        }
    }

    override suspend fun clearHistory() {
        withContext(dispatcher) { dao.clearAll() }
    }

    private fun ActivityResultEntity.toDomain() = ActivityResult(
        type = type,
        timestamp = timestamp,
        score = score,
        correctCount = correctCount,
        totalCount = totalCount,
        durationMs = durationMs
    )
}
