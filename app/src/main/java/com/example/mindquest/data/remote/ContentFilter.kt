package com.example.mindquest.data.remote

/**
 * Open Trivia DB's category whitelist ([TriviaCategories.SAFE_CATEGORIES]) only screens out
 * entire categories (e.g. Politics, History). It cannot stop an individual question *inside*
 * an allowed category from touching age-inappropriate content — found on a real device during
 * testing, a "General Knowledge" question asked what vodka is commonly called. This keyword
 * blocklist is the second, question-level layer of the kid-safe content sourcing design.
 */
object ContentFilter {
    private val blockedKeywords = listOf(
        // Alcohol / tobacco
        "vodka", "whisky", "whiskey", "wine", "beer", "rum", "gin", "tequila",
        "liquor", "cocktail", "alcohol", "brewery", "cigarette", "tobacco", "smoking",
        // Drugs
        "cocaine", "heroin", "marijuana", "cannabis", "narcotic",
        // Weapons / violence / self-harm
        "firearm", "pistol", "rifle", "bomb", "murder", "suicide", "torture",
        // Gambling
        "gambling", "casino",
        // Sexual content
        "sexual", "porn", "rape"
    )

    fun isSafe(vararg texts: String): Boolean =
        texts.none { text -> blockedKeywords.any { text.contains(it, ignoreCase = true) } }
}
