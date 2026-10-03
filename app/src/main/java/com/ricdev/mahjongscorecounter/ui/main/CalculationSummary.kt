package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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

private val SummaryMinHeight = 84.dp

/**
 * Live preview of the round being entered. Holds a steady height across its states so the
 * entry form doesn't jump while the user types.
 *
 * A blank amount is still being filled in, not a mistake, so it shows the hint rather than an error.
 */
@Composable
fun CalculationSummary(
    state: PreviewState,
    amountEntered: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SummaryMinHeight)
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.CenterStart,
    ) {
        when {
            state is PreviewState.Valid -> PendingChanges(state.input, state.result)
            state is PreviewState.Invalid &&
                (amountEntered || state.error != ValidationError.AmountBelowOne) ->
                ErrorLine(state.error)
            else -> HintLine()
        }
    }
}

@Composable
private fun PendingChanges(input: RoundInput, result: RoundResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preview_subtitle),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DeltaStrip(deltas = result.deltas, winner = input.winner)
    }
}

/** A dashed outline marks the round as not yet filled in. */
@Composable
private fun HintLine() {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SummaryMinHeight)
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.preview_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorLine(error: ValidationError) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SummaryMinHeight)
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = stringResource(error.toResId()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
internal fun deltaColor(delta: Int): Color {
    val mahjongColors = MaterialTheme.mahjongColors
    return when {
        delta > 0 -> mahjongColors.deltaPositive
        delta < 0 -> mahjongColors.deltaNegative
        else -> mahjongColors.deltaNeutral
    }
}

internal fun ValidationError.toResId(): Int = when (this) {
    ValidationError.AmountBelowOne -> R.string.error_amount_below_one
    ValidationError.AmountAboveMaximum -> R.string.error_amount_above_max
    ValidationError.PayerRequired -> R.string.error_payer_required
    ValidationError.PayerForbiddenForSelfDraw -> R.string.error_payer_forbidden_self_draw
    ValidationError.WinnerIsPayer -> R.string.error_winner_is_payer
}

@Preview(showBackground = true, widthDp = 360, backgroundColor = 0xFFF8FAF6)
@Composable
private fun CalculationSummaryEmptyPreview() {
    MahjongScoreCounterTheme {
        CalculationSummary(
            state = PreviewState.Empty,
            amountEntered = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, backgroundColor = 0xFFF8FAF6)
@Composable
private fun CalculationSummaryInvalidPreview() {
    MahjongScoreCounterTheme {
        CalculationSummary(
            state = PreviewState.Invalid(ValidationError.PayerRequired),
            amountEntered = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, backgroundColor = 0xFFF8FAF6)
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
            amountEntered = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
