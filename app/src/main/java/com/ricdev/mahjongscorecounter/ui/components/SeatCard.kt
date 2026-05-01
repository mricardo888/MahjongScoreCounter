package com.ricdev.mahjongscorecounter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SeatCard(
    seat: Seat,
    total: Int,
    lastDelta: Int?,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val formatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    val mahjongColors = MaterialTheme.mahjongColors
    val seatLabel = stringResource(seat.labelResId())
    val totalLabel = formatter.format(total)
    val lastDeltaLabel = lastDelta?.takeIf { it != 0 }?.let { delta ->
        val sign = if (delta > 0) "+" else ""
        "$sign${formatter.format(delta)}"
    }
    val scoreDescription = if (lastDeltaLabel == null) {
        stringResource(R.string.accessibility_seat_score, seatLabel, totalLabel)
    } else {
        stringResource(R.string.accessibility_seat_score_with_delta, seatLabel, totalLabel, lastDeltaLabel)
    }
    val selectedDescription = if (highlighted) {
        " ${stringResource(R.string.accessibility_selected_winner)}"
    } else {
        ""
    }
    val clickLabel = stringResource(R.string.accessibility_select_winner, seatLabel)
    val border = if (highlighted) {
        BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    Surface(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = scoreDescription + selectedDescription
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = clickLabel,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (highlighted) 6.dp else 2.dp,
        shadowElevation = if (highlighted) 4.dp else 1.dp,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = seatLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = totalLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (lastDelta != null && lastDelta != 0) {
                val deltaColor: Color = when {
                    lastDelta > 0 -> mahjongColors.deltaPositive
                    lastDelta < 0 -> mahjongColors.deltaNegative
                    else -> mahjongColors.deltaNeutral
                }
                Text(
                    text = lastDeltaLabel.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = deltaColor,
                )
            }
        }
    }
}

private fun Seat.labelResId(): Int = when (this) {
    Seat.EAST -> R.string.seat_east
    Seat.SOUTH -> R.string.seat_south
    Seat.WEST -> R.string.seat_west
    Seat.NORTH -> R.string.seat_north
}

@Preview(showBackground = true, widthDp = 140)
@Composable
private fun SeatCardHighlightedPreview() {
    MahjongScoreCounterTheme {
        SeatCard(
            seat = Seat.EAST,
            total = 768,
            lastDelta = 256,
            highlighted = true,
        )
    }
}

@Preview(showBackground = true, widthDp = 140)
@Composable
private fun SeatCardIdlePreview() {
    MahjongScoreCounterTheme {
        SeatCard(
            seat = Seat.SOUTH,
            total = -256,
            lastDelta = -256,
            highlighted = false,
        )
    }
}
