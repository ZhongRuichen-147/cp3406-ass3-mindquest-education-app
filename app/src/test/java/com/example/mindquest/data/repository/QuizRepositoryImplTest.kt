package com.example.mindquest.data.repository

import com.example.mindquest.data.NetworkResult
import com.example.mindquest.data.local.dao.QuizQuestionDao
import com.example.mindquest.data.local.entity.QuizQuestionEntity
import com.example.mindquest.data.remote.TriviaApi
import com.example.mindquest.data.remote.TriviaQuestionDto
import com.example.mindquest.data.remote.TriviaResponseDto
import com.example.mindquest.data.settings.Difficulty
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class QuizRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val api = mockk<TriviaApi>()
    private val dao = mockk<QuizQuestionDao>(relaxUnitFun = true)
    private val repository = QuizRepositoryImpl(api, dao, dispatcher)

    @Test
    fun `getQuizQuestions returns cached questions after a successful network sync`() = runTest(dispatcher) {
        val networkQuestion = TriviaQuestionDto(
            category = "Animals",
            difficulty = "easy",
            question = "What sound does a cat make?",
            correct_answer = "Meow",
            incorrect_answers = listOf("Woof", "Moo", "Quack")
        )
        coEvery { api.getQuestions(any(), any(), any(), any()) } returns
            Response.success(TriviaResponseDto(response_code = 0, results = listOf(networkQuestion)))

        val cachedEntity = QuizQuestionEntity(
            id = 1,
            category = "Animals",
            difficulty = "easy",
            question = "What sound does a cat make?",
            correctAnswer = "Meow",
            incorrectAnswersJson = Json.encodeToString(listOf("Woof", "Moo", "Quack")),
            fetchedAt = 0L
        )
        coEvery { dao.getRandomQuestions("easy", 20) } returns listOf(cachedEntity)

        val result = repository.getQuizQuestions(Difficulty.EASY, 5)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals(1, questions.size)
        assertEquals("Meow", questions.first().correctAnswer)
        assertTrue(questions.first().options.containsAll(listOf("Meow", "Woof", "Moo", "Quack")))
    }

    @Test
    fun `getQuizQuestions falls back to the Room cache when the network call fails`() = runTest(dispatcher) {
        coEvery { api.getQuestions(any(), any(), any(), any()) } throws IOException("offline")

        val cachedEntity = QuizQuestionEntity(
            id = 2,
            category = "Animals",
            difficulty = "easy",
            question = "Cached question?",
            correctAnswer = "Yes",
            incorrectAnswersJson = Json.encodeToString(listOf("No")),
            fetchedAt = 0L
        )
        coEvery { dao.getRandomQuestions("easy", 20) } returns listOf(cachedEntity)

        val result = repository.getQuizQuestions(Difficulty.EASY, 5)

        assertTrue(result is NetworkResult.Success)
        assertEquals("Cached question?", (result as NetworkResult.Success).data.first().question)
    }

    @Test
    fun `getQuizQuestions returns an error when the network fails and the cache is empty`() = runTest(dispatcher) {
        coEvery { api.getQuestions(any(), any(), any(), any()) } throws IOException("offline")
        coEvery { dao.getRandomQuestions("easy", 20) } returns emptyList()

        val result = repository.getQuizQuestions(Difficulty.EASY, 5)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `syncFromNetwork skips a question that fails the kid-safe content filter`() = runTest(dispatcher) {
        val unsafeQuestion = TriviaQuestionDto(
            category = "General Knowledge",
            difficulty = "easy",
            question = "Vodka is a distilled beverage traditionally made from what?",
            correct_answer = "Grain",
            incorrect_answers = listOf("Grapes", "Sugarcane", "Rice")
        )
        coEvery { api.getQuestions(any(), any(), any(), any()) } returns
            Response.success(TriviaResponseDto(response_code = 0, results = listOf(unsafeQuestion)))
        coEvery { dao.getRandomQuestions("easy", 20) } returns emptyList()

        repository.getQuizQuestions(Difficulty.EASY, 5)

        coVerify { dao.insertAll(emptyList()) }
    }

    @Test
    fun `getQuizQuestions retires a cached question that fails the content filter and does not serve it`() =
        runTest(dispatcher) {
            coEvery { api.getQuestions(any(), any(), any(), any()) } throws IOException("offline")

            val unsafeEntity = QuizQuestionEntity(
                id = 3,
                category = "General Knowledge",
                difficulty = "easy",
                question = "What is vodka commonly called?",
                correctAnswer = "Firewater",
                incorrectAnswersJson = Json.encodeToString(listOf("Water", "Juice", "Milk")),
                fetchedAt = 0L
            )
            val safeEntity = QuizQuestionEntity(
                id = 4,
                category = "Animals",
                difficulty = "easy",
                question = "What sound does a dog make?",
                correctAnswer = "Woof",
                incorrectAnswersJson = Json.encodeToString(listOf("Meow", "Moo", "Quack")),
                fetchedAt = 0L
            )
            coEvery { dao.getRandomQuestions("easy", 20) } returns listOf(unsafeEntity, safeEntity)

            val result = repository.getQuizQuestions(Difficulty.EASY, 5)

            assertTrue(result is NetworkResult.Success)
            val questions = (result as NetworkResult.Success).data
            assertEquals(1, questions.size)
            assertEquals("Woof", questions.first().correctAnswer)
            coVerify { dao.deleteByIds(listOf(3L)) }
        }
}
