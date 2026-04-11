package com.ricdev.mahjongscorecounter.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNegative
import com.ricdev.mahjongscorecounter.ui.theme.DeltaNeutral
import com.ricdev.mahjongscorecounter.ui.theme.DeltaPositive
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SeatCard(
    seat: Seat,
    total: Int,
    lastDelta: Int?,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    val border = if (highlighted) {
        BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    Surface(
        modifier = modifier,
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
                text = stringResource(seat.labelResId()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = formatter.format(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (lastDelta != null && lastDelta != 0) {
                val deltaColor: Color = when {
                    lastDelta > 0 -> DeltaPositive
                    lastDelta < 0 -> DeltaNegative
                    else -> DeltaNeutral
                }
                val sign = if (lastDelta > 0) "+" else ""
                Text(
                    text = "$sign${formatter.format(lastDelta)}",
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
