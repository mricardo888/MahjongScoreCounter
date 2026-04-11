package com.ricdev.mahjongscorecounter.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.main.labelResId
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

data class SeatStats(
    val seat: Seat,
    val total: Int,
    val handsWon: Int,
    val bestRound: Int,
    val worstRound: Int,
)

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val history = gameState.history

    val stats = remember(history) { derivePerSeatStats(history) }

    if (history.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.stats_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = stats,
            key = { it.seat.name },
        ) { seatStats ->
            StatsCard(seatStats)
        }
    }
}

@Composable
private fun StatsCard(stats: SeatStats) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(stats.seat.labelResId()),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            StatRow(label = stringResource(R.string.stats_total), value = stats.total)
            StatRow(label = stringResource(R.string.stats_hands_won), value = stats.handsWon)
            StatRow(label = stringResource(R.string.stats_best_round), value = stats.bestRound)
            StatRow(label = stringResource(R.string.stats_worst_round), value = stats.worstRound)
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

fun derivePerSeatStats(history: List<CommittedRound>): List<SeatStats> {
    return Seat.entries.map { seat ->
        val deltas = history.map { it.result.deltas[seat] ?: 0 }
        val total = deltas.sum()
        val handsWon = history.count { it.input.winner == seat }
        val best = deltas.maxOrNull() ?: 0
        val worst = deltas.minOrNull() ?: 0
        SeatStats(seat, total, handsWon, best, worst)
    }
}
