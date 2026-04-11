package com.ricdev.mahjongscorecounter.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.main.labelResId
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNegative
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNeutral
import com.ricdev.mahjongscorecounter.ui.theme.DeltaPositive
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun LogsScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val history = gameState.history

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = { viewModel.undoLast() },
            enabled = history.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_undo))
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.logs_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = history.asReversed(),
                    key = { round -> round.timestampMillis },
                ) { round ->
                    LogEntry(round = round)
                }
            }
        }
    }
}

@Composable
private fun LogEntry(round: CommittedRound) {
    val timeFormatter = DateFormat.getTimeInstance()
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = timeFormatter.format(Date(round.timestampMillis)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val title = when (round.input.winType) {
                WinType.SELF_DRAW -> stringResource(
                    R.string.logs_entry_self_draw,
                    stringResource(round.input.winner.labelResId()),
                    round.input.fanCount,
                )
                WinType.DISCARD_WIN -> stringResource(
                    R.string.logs_entry_discard,
                    stringResource(round.input.winner.labelResId()),
                    stringResource((round.input.discarder ?: round.input.winner).labelResId()),
                    round.input.fanCount,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Seat.entries.forEach { seat ->
                    val delta = round.result.deltas[seat] ?: 0
                    DeltaChip(seat = seat, delta = delta)
                }
            }
        }
    }
}

@Composable
private fun DeltaChip(seat: Seat, delta: Int) {
    val color: Color = when {
        delta > 0 -> DeltaPositive
        delta < 0 -> DeltaNegative
        else -> DeltaNeutral
    }
    val sign = if (delta > 0) "+" else ""
    Box(
        modifier = Modifier
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
