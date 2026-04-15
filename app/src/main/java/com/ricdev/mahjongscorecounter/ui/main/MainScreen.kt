package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel
import com.ricdev.mahjongscorecounter.viewmodel.PreviewState

@Composable
fun MainScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val form by viewModel.formState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val preview by viewModel.preview.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TableScoreboard(
            totals = gameState.totals,
            lastRound = gameState.history.lastOrNull(),
            highlightedWinner = form.winner,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        RoundEntryForm(
            form = form,
            rules = gameState.rules,
            onWinnerSelected = viewModel::selectWinner,
            onWinTypeSelected = viewModel::selectWinType,
            onFanChange = viewModel::setFan,
            onFlowerChange = viewModel::setFlowerCount,
            onAnimalChange = viewModel::setAnimalCount,
            onTaiChange = viewModel::setTai,
            onHanChange = viewModel::setHan,
            onFuChange = viewModel::setFu,
            onDiscarderSelected = viewModel::selectDiscarder,
        )

        CalculationSummary(state = preview)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { viewModel.commitRound() },
                enabled = preview is PreviewState.Valid,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_commit_round))
            }
            OutlinedButton(
                onClick = { viewModel.undoLast() },
                enabled = gameState.history.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_undo))
            }
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_reset))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.resetGame()
                }) {
                    Text(stringResource(R.string.reset_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
