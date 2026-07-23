package com.example.mindquest.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mindquest.data.local.entity.ActivityType
import com.example.mindquest.domain.ActivityResult
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Your progress", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(label = "Games played", value = "${uiState.totalGames}", modifier = Modifier.weight(1f))
            StatTile(label = "Day streak", value = "${uiState.streakDays}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(label = "Quiz accuracy", value = "${uiState.averageAccuracyPercent}%", modifier = Modifier.weight(1f))
            StatTile(label = "Best quiz score", value = "${uiState.bestQuizScore}", modifier = Modifier.weight(1f))
        }

        Text(text = "Recent activity", style = MaterialTheme.typography.titleLarge)

        if (uiState.recent.isEmpty()) {
            Text(
                text = "No activity yet — play a Quiz or Memory Match to see your progress here.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.recent) { result -> ActivityResultRow(result) }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

private val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

@Composable
private fun ActivityResultRow(result: ActivityResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (result.type == ActivityType.QUIZ) Icons.Filled.Quiz else Icons.Filled.Extension,
                contentDescription = null
            )
            Column {
                Text(
                    text = if (result.type == ActivityType.QUIZ) "Quiz" else "Memory Match",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Score ${result.score} · ${dateFormat.format(Date(result.timestamp))}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
