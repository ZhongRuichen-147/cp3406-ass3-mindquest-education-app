package com.example.mindquest.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mindquest.data.settings.Difficulty
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Difficulty", style = MaterialTheme.typography.titleLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Difficulty.entries.forEachIndexed { index, difficulty ->
                SegmentedButton(
                    selected = uiState.settings.difficulty == difficulty,
                    onClick = { viewModel.setDifficulty(difficulty) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = Difficulty.entries.size)
                ) {
                    Text(difficulty.label)
                }
            }
        }

        SettingSwitchRow(
            label = "Sound effects",
            checked = uiState.settings.soundEnabled,
            onCheckedChange = viewModel::setSoundEnabled
        )
        SettingSwitchRow(
            label = "Dark theme",
            checked = uiState.settings.darkTheme,
            onCheckedChange = viewModel::setDarkTheme
        )

        PrivacyInfoCard()

        OutlinedButton(
            onClick = viewModel::requestClearData,
            modifier = Modifier.fillMaxWidth().testTag("clear_data_button")
        ) {
            Text("Clear my data")
        }

        if (uiState.dataClearedMessage) {
            Text(
                text = "All your data has been cleared.",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("data_cleared_message")
            )
            LaunchedEffect(uiState.dataClearedMessage) {
                delay(3000)
                viewModel.dismissDataClearedMessage()
            }
        }
    }

    if (uiState.isParentGateVisible) {
        ParentGateDialog(
            uiState = uiState,
            onAnswerChange = viewModel::updateGateAnswer,
            onConfirm = viewModel::submitGateAnswer,
            onDismiss = viewModel::dismissParentGate
        )
    }
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PrivacyInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "What we store", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "MindQuest keeps your scores, streak and these settings on this device only. " +
                    "There's no account, no location, and no ads — you can erase everything below at any time.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ParentGateDialog(
    uiState: SettingsUiState,
    onAnswerChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ask a grown-up") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Solve this to clear all data: ${uiState.gateA} + ${uiState.gateB} = ?")
                OutlinedTextField(
                    value = uiState.gateAnswer,
                    onValueChange = onAnswerChange,
                    isError = uiState.gateError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.testTag("parent_gate_input")
                )
                if (uiState.gateError) {
                    Text("That's not quite right — try again.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, modifier = Modifier.testTag("parent_gate_confirm")) { Text("Confirm") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
