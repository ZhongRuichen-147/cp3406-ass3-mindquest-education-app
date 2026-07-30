package com.example.mindquest.ui.activityscreen.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun MemoryMatchScreen(viewModel: MemoryMatchViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Pairs found: ${uiState.matchedPairs}/${uiState.totalPairs}  ·  Moves: ${uiState.moves}",
            style = MaterialTheme.typography.bodyLarge
        )

        if (uiState.isFinished) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "All pairs matched! 🎉", style = MaterialTheme.typography.titleLarge)
                    Text(text = "It took you ${uiState.moves} moves.", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = viewModel::startNewGame, modifier = Modifier.testTag("memory_play_again")) {
                        Text("Play again")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.cards, key = { it.id }) { card ->
                    MemoryCardView(
                        card = card,
                        onClick = { viewModel.onCardClick(uiState.cards.indexOf(card)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryCardView(card: MemoryCard, onClick: () -> Unit) {
    val showFace = card.isFlipped || card.isMatched
    val rotation by animateFloatAsState(
        targetValue = if (showFace) 180f else 0f,
        animationSpec = tween(300),
        label = "cardFlip"
    )

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .testTag("memory_card")
            .graphicsLayer { rotationY = rotation },
        onClick = onClick,
        enabled = !card.isMatched,
        colors = CardDefaults.cardColors(
            containerColor = if (card.isMatched) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (rotation > 90f) {
                Text(text = card.symbol, style = MaterialTheme.typography.titleLarge)
                // Matched cards get a checkmark too — the tertiaryContainer/secondaryContainer
                // color swap alone isn't a reliable cue for colorblind players.
                if (card.isMatched) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Matched",
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    )
                }
            } else {
                Text(text = "?", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
