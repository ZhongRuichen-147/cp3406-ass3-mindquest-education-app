package com.example.mindquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

/** Exercises the real app + bottom navigation bar end to end. */
class NavigationInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigationSwitchesBetweenTopLevelScreens() {
        composeTestRule.onNodeWithText("Choose an activity").assertIsDisplayed()

        composeTestRule.onNodeWithText("Stats").performClick()
        composeTestRule.onNodeWithText("Your progress").assertIsDisplayed()

        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.onNodeWithText("Choose an activity").assertIsDisplayed()

        composeTestRule.onNodeWithText("Play").performClick()
        composeTestRule.onNodeWithText("Quiz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Memory Match").assertIsDisplayed()
    }
}
