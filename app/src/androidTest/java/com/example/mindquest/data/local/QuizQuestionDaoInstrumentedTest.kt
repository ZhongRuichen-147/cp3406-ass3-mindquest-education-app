package com.example.mindquest.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mindquest.data.local.entity.QuizQuestionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies the real Room (question, difficulty) unique index actually dedupes on insert. */
@RunWith(AndroidJUnit4::class)
class QuizQuestionDaoInstrumentedTest {

    private lateinit var db: MindQuestDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, MindQuestDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertingTheSameQuestionTwiceKeepsOnlyOneRow() = runBlocking {
        val dao = db.quizQuestionDao()
        val question = QuizQuestionEntity(
            category = "Animals",
            difficulty = "easy",
            question = "What sound does a cat make?",
            correctAnswer = "Meow",
            incorrectAnswersJson = "[]",
            fetchedAt = 1000L
        )

        dao.insertAll(listOf(question))
        dao.insertAll(listOf(question.copy(fetchedAt = 2000L)))

        assertEquals(1, dao.countByDifficulty("easy"))
    }

    @Test
    fun differentDifficultyOfTheSameQuestionIsKeptSeparately() = runBlocking {
        val dao = db.quizQuestionDao()
        val easy = QuizQuestionEntity(
            category = "Animals", difficulty = "easy", question = "What sound does a cat make?",
            correctAnswer = "Meow", incorrectAnswersJson = "[]", fetchedAt = 1000L
        )
        val medium = easy.copy(difficulty = "medium")

        dao.insertAll(listOf(easy, medium))

        assertEquals(1, dao.countByDifficulty("easy"))
        assertEquals(1, dao.countByDifficulty("medium"))
    }
}
