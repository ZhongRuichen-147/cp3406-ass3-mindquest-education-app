package com.example.mindquest.ui.activityscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.mindquest.ui.activityscreen.memory.MemoryMatchScreen
import com.example.mindquest.ui.activityscreen.quiz.QuizScreen

private val TAB_TITLES = listOf("Quiz", "Memory Match")

@Composable
fun ActivityScreen(startTab: String? = null) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(if (startTab == "memory") 1 else 0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            TAB_TITLES.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    modifier = Modifier.testTag("activity_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> QuizScreen()
            1 -> MemoryMatchScreen()
        }
    }
}
