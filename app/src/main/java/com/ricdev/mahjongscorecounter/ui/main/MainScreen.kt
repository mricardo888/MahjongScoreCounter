package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNegative
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNeutral
import com.ricdev.mahjongscorecounter.ui.theme.DeltaPositive
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel
import com.ricdev.mahjongscorecounter.viewmodel.PreviewState
import java.text.DateFormat
import java.util.Date

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
            onSeatSelected = viewModel::selectWinner,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        RoundEntryForm(
            form = form,
            onWinnerSelected = viewModel::selectWinner,
            onWinTypeSelected = viewModel::selectWinType,
            onAmountChange = viewModel::setAmount,
            onPayerSelected = viewModel::selectPayer,
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

@Composable
fun RecentRoundsScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RecentRounds(history = gameState.history)
    }
}

@Composable
private fun RecentRounds(history: List<CommittedRound>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.recent_rounds_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.logs_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            history.takeLast(5).asReversed().forEach { round ->
                RecentRoundCard(round = round)
            }
        }
    }
}

@Composable
private fun RecentRoundCard(round: CommittedRound) {
    val timeFormatter = remember { DateFormat.getTimeInstance(DateFormat.SHORT) }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = timeFormatter.format(Date(round.timestampMillis)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val winnerLabel = stringResource(round.input.winner.labelResId())
            val title = when (round.input.winType) {
                WinType.SELF_DRAW -> stringResource(
                    R.string.logs_entry_self_draw,
                    winnerLabel,
                    round.input.amount,
                )
                WinType.DISCARD_WIN -> {
                    val payerLabel = stringResource(
                        (round.input.payer ?: round.input.winner).labelResId()
                    )
                    stringResource(
                        R.string.logs_entry_discard,
                        winnerLabel,
                        payerLabel,
                        round.input.amount,
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Seat.entries.chunked(2).forEach { rowSeats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowSeats.forEach { seat ->
                            val delta = round.result.deltas[seat] ?: 0
                            DeltaChip(
                                seat = seat,
                                delta = delta,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeltaChip(
    seat: Seat,
    delta: Int,
    modifier: Modifier = Modifier,
) {
    val color: Color = when {
        delta > 0 -> DeltaPositive
        delta < 0 -> DeltaNegative
        else -> DeltaNeutral
    }
    val sign = if (delta > 0) "+" else ""
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = "${stringResource(seat.labelResId())} $sign$delta",
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}
