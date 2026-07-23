package com.example.mindquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.ui.navigation.MindQuestApp
import com.example.mindquest.ui.theme.MindQuestTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsRepository: SettingsRepository = koinInject()
            val settings by settingsRepository.settings.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()

            MindQuestTheme(darkTheme = settings?.darkTheme ?: systemDark) {
                MindQuestApp()
            }
        }
    }
}
