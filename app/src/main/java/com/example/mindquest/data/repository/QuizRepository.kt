package com.example.mindquest.data.repository

import com.example.mindquest.data.NetworkResult
import com.example.mindquest.data.local.dao.QuizQuestionDao
import com.example.mindquest.data.local.entity.QuizQuestionEntity
import com.example.mindquest.data.remote.ContentFilter
import com.example.mindquest.data.remote.TriviaApi
import com.example.mindquest.data.remote.TriviaCategories
import com.example.mindquest.data.settings.Difficulty
import com.example.mindquest.domain.QuizQuestion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface QuizRepository {
    suspend fun getQuizQuestions(difficulty: Difficulty, count: Int = 5): NetworkResult<List<QuizQuestion>>
    suspend fun clearCache()
}

/**
 * Fetches fresh questions from Open Trivia DB and caches them in Room, then always serves
 * from the Room cache. This keeps the Quiz activity usable offline after the first sync and
 * avoids a network round-trip (and any accompanying identifiers) on every single play session.
 */
class QuizRepositoryImpl(
    private val api: TriviaApi,
    private val dao: QuizQuestionDao,
    private val dispatcher: CoroutineDispatcher
) : QuizRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getQuizQuestions(difficulty: Difficulty, count: Int): NetworkResult<List<QuizQuestion>> {
        return withContext(dispatcher) {
            val syncError = syncFromNetwork(difficulty)

            // Over-fetch: some cached rows may predate the content filter (or a keyword miss
            // slipped one through at sync time), so pull extra and filter again before serving.
            val candidates = dao.getRandomQuestions(difficulty.triviaValue, count * CACHE_OVER_FETCH_FACTOR)
            val (safe, unsafe) = candidates.partition { it.isKidSafe() }
            if (unsafe.isNotEmpty()) {
                dao.deleteByIds(unsafe.map { it.id })
            }
            val cached = safe.take(count)

            if (cached.isNotEmpty()) {
                NetworkResult.Success(cached.map { it.toDomain() })
            } else {
                NetworkResult.Error(
                    syncError ?: "No questions available offline yet — connect once to download some."
                )
            }
        }
    }

    private suspend fun syncFromNetwork(difficulty: Difficulty): String? {
        return try {
            val category = TriviaCategories.SAFE_CATEGORIES.random()
            val response = api.getQuestions(
                amount = 10,
                category = category,
                difficulty = difficulty.triviaValue
            )
            val body = response.body()
            if (response.isSuccessful && body != null && body.response_code == 0) {
                val entities = body.results
                    .filter { dto ->
                        ContentFilter.isSafe(
                            dto.question.unescapeHtml(),
                            dto.correct_answer.unescapeHtml(),
                            *dto.incorrect_answers.map { it.unescapeHtml() }.toTypedArray()
                        )
                    }
                    .map { dto ->
                        QuizQuestionEntity(
                            category = dto.category.unescapeHtml(),
                            difficulty = dto.difficulty,
                            question = dto.question.unescapeHtml(),
                            correctAnswer = dto.correct_answer.unescapeHtml(),
                            incorrectAnswersJson = json.encodeToString(
                                dto.incorrect_answers.map { it.unescapeHtml() }
                            ),
                            fetchedAt = System.currentTimeMillis()
                        )
                    }
                dao.insertAll(entities)
                null
            } else {
                "HTTP ${response.code()}"
            }
        } catch (e: Exception) {
            e.message ?: "Network unavailable"
        }
    }

    override suspend fun clearCache() {
        withContext(dispatcher) { dao.clearAll() }
    }

    private fun QuizQuestionEntity.toDomain(): QuizQuestion {
        val incorrect = json.decodeFromString<List<String>>(incorrectAnswersJson)
        return QuizQuestion(
            question = question,
            correctAnswer = correctAnswer,
            options = (incorrect + correctAnswer).shuffled()
        )
    }

    private fun QuizQuestionEntity.isKidSafe(): Boolean {
        val incorrect = json.decodeFromString<List<String>>(incorrectAnswersJson)
        return ContentFilter.isSafe(question, correctAnswer, *incorrect.toTypedArray())
    }

    private companion object {
        const val CACHE_OVER_FETCH_FACTOR = 4
    }
}

/** Open Trivia DB returns HTML-entity-encoded text by default. */
private fun String.unescapeHtml(): String = this
    .replace("&quot;", "\"")
    .replace("&#039;", "'")
    .replace("&rsquo;", "'")
    .replace("&ldquo;", "\"")
    .replace("&rdquo;", "\"")
    .replace("&eacute;", "é")
    .replace("&uuml;", "ü")
    .replace("&amp;", "&")
