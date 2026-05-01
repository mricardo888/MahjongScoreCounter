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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors
import com.ricdev.mahjongscorecounter.viewmodel.PreviewState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculationSummary(
    state: PreviewState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        PreviewState.Empty -> EmptyPrompt(modifier)
        is PreviewState.Invalid -> ErrorCard(state.error, modifier)
        is PreviewState.Valid -> ValidPreview(state.input, state.result, modifier)
    }
}

@Composable
private fun EmptyPrompt(modifier: Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
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
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
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
private fun ValidPreview(input: RoundInput, result: RoundResult, modifier: Modifier) {
    val formatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val title = when (input.winType) {
                WinType.SELF_DRAW -> stringResource(
                    R.string.preview_title_self_draw,
                    formatter.format(input.amount),
                )
                WinType.DISCARD_WIN -> stringResource(
                    R.string.preview_title_discard,
                    formatter.format(input.amount),
                )
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
internal fun DeltaRow(seat: Seat, delta: Int) {
    val mahjongColors = MaterialTheme.mahjongColors
    val formatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    val color: Color = when {
        delta > 0 -> mahjongColors.deltaPositive
        delta < 0 -> mahjongColors.deltaNegative
        else -> mahjongColors.deltaNeutral
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
            text = "$sign${formatter.format(delta)}",
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
    ValidationError.AmountBelowOne -> R.string.error_amount_below_one
    ValidationError.AmountAboveMaximum -> R.string.error_amount_above_max
    ValidationError.PayerRequired -> R.string.error_payer_required
    ValidationError.PayerForbiddenForSelfDraw -> R.string.error_payer_forbidden_self_draw
    ValidationError.WinnerIsPayer -> R.string.error_winner_is_payer
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
            state = PreviewState.Invalid(ValidationError.PayerRequired),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CalculationSummaryValidPreview() {
    MahjongScoreCounterTheme {
        CalculationSummary(
            state = PreviewState.Valid(
                input = RoundInput(
                    winner = Seat.EAST,
                    winType = WinType.SELF_DRAW,
                    amount = 3,
                ),
                result = RoundResult(
                    deltas = mapOf(
                        Seat.EAST to 9,
                        Seat.SOUTH to -3,
                        Seat.WEST to -3,
                        Seat.NORTH to -3,
                    ),
                    winner = Seat.EAST,
                    winType = WinType.SELF_DRAW,
                ),
            ),
        )
    }
}
