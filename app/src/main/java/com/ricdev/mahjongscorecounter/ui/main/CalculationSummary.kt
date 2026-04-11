package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNegative
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNeutral
import com.ricdev.mahjongscorecounter.ui.theme.DeltaPositive
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.viewmodel.PreviewState

@Composable
fun CalculationSummary(
    state: PreviewState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        PreviewState.Empty -> EmptyPrompt(modifier)
        is PreviewState.Invalid -> ErrorCard(state.error, modifier)
        is PreviewState.Valid -> ValidPreview(state.result, modifier)
    }
}

@Composable
private fun EmptyPrompt(modifier: Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.preview_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun ErrorCard(error: ValidationError, modifier: Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Text(
            text = stringResource(error.toResId()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun ValidPreview(result: RoundResult, modifier: Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val title = when (result.winType) {
                WinType.SELF_DRAW -> stringResource(R.string.preview_title_self_draw, result.fanCount)
                WinType.DISCARD_WIN -> stringResource(R.string.preview_title_discard, result.fanCount)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.preview_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Seat.entries.forEach { seat ->
                val delta = result.deltas[seat] ?: 0
                if (delta == 0) return@forEach
                DeltaRow(seat = seat, delta = delta)
            }
        }
    }
}

@Composable
private fun DeltaRow(seat: Seat, delta: Int) {
    val color: Color = when {
        delta > 0 -> DeltaPositive
        delta < 0 -> DeltaNegative
        else -> DeltaNeutral
    }
    val sign = if (delta > 0) "+" else ""
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(seat.labelResId()),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "$sign$delta",
            style = MaterialTheme.typography.bodyLarge,
            color = color,
        )
    }
}

internal fun Seat.labelResId(): Int = when (this) {
    Seat.EAST -> R.string.seat_east
    Seat.SOUTH -> R.string.seat_south
    Seat.WEST -> R.string.seat_west
    Seat.NORTH -> R.string.seat_north
}

internal fun ValidationError.toResId(): Int = when (this) {
    ValidationError.NoWinner -> R.string.error_no_winner
    ValidationError.FanBelowOne -> R.string.error_fan_below_one
    ValidationError.DiscarderRequired -> R.string.error_discarder_required
    ValidationError.DiscarderForbiddenForSelfDraw -> R.string.error_discarder_forbidden_self_draw
    ValidationError.WinnerIsDiscarder -> R.string.error_winner_is_discarder
    ValidationError.NegativeBase -> R.string.error_negative_base
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CalculationSummaryEmptyPreview() {
    MahjongScoreCounterTheme {
        CalculationSummary(state = PreviewState.Empty)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CalculationSummaryInvalidPreview() {
    MahjongScoreCounterTheme {
        CalculationSummary(
            state = PreviewState.Invalid(ValidationError.DiscarderRequired),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CalculationSummaryValidPreview() {
    MahjongScoreCounterTheme {
        CalculationSummary(
            state = PreviewState.Valid(
                RoundResult(
                    deltas = mapOf(
                        Seat.EAST to 48,
                        Seat.SOUTH to -16,
                        Seat.WEST to -16,
                        Seat.NORTH to -16,
                    ),
                    winner = Seat.EAST,
                    winType = WinType.SELF_DRAW,
                    fanCount = 2,
                    perLoserAmount = 16,
                    totalAmount = 48,
                ),
            ),
        )
    }
}
