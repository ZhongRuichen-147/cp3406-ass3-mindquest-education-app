package com.example.mindquest.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open Trivia DB (opentdb.com) — free, no API key. Only ever asked for content
 * (questions/answers), never sent any user or device data: there is nothing about
 * the child using the app for this endpoint to leak.
 */
interface TriviaApi {
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String = "multiple"
    ): Response<TriviaResponseDto>
}

/** Kid-safe category whitelist — deliberately excludes categories like Politics, History wars, etc. */
object TriviaCategories {
    const val ANIMALS = 27
    const val SCIENCE_NATURE = 17
    const val GENERAL_KNOWLEDGE = 9

    val SAFE_CATEGORIES = listOf(ANIMALS, SCIENCE_NATURE, GENERAL_KNOWLEDGE)
}
