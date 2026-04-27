package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.components.SeatCard
import com.ricdev.mahjongscorecounter.ui.theme.FeltGreen
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.WoodBrown

@Composable
fun TableScoreboard(
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    highlightedWinner: Seat?,
    onSeatSelected: (Seat) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val side = minOf(maxWidth, maxHeight)
        val cardWidth = side * 0.32f
        Surface(
            modifier = Modifier.size(side),
            shape = MaterialTheme.shapes.large,
            color = WoodBrown,
            shadowElevation = 8.dp,
        ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = FeltGreen,
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    Seat.entries.forEach { seat ->
                        val alignment: Alignment = when (seat) {
                            Seat.EAST -> Alignment.CenterEnd
                            Seat.SOUTH -> Alignment.BottomCenter
                            Seat.WEST -> Alignment.CenterStart
                            Seat.NORTH -> Alignment.TopCenter
                        }
                        SeatCard(
                            seat = seat,
                            total = totals[seat] ?: 0,
                            lastDelta = lastRound?.result?.deltas?.get(seat),
                            highlighted = seat == highlightedWinner,
                            onClick = { onSeatSelected(seat) },
                            modifier = Modifier
                                .align(alignment)
                                .width(cardWidth),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 360)
@Composable
private fun TableScoreboardPreview() {
    MahjongScoreCounterTheme {
        TableScoreboard(
            totals = mapOf(
                Seat.EAST to 768,
                Seat.SOUTH to -256,
                Seat.WEST to -256,
                Seat.NORTH to -256,
            ),
            lastRound = null,
            highlightedWinner = Seat.EAST,
            onSeatSelected = {},
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
    }
}
