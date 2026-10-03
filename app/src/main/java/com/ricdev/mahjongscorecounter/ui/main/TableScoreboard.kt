package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.components.SeatCard
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.ScoreNumerals
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors

private const val TILE_WIDTH_FRACTION = 0.34f
private const val TILE_HEIGHT_FRACTION = 0.30f
private const val HUB_FRACTION = 0.24f

@Composable
fun TableScoreboard(
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    roundsPlayed: Int,
    highlightedWinner: Seat?,
    onSeatSelected: (Seat) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val mahjongColors = MaterialTheme.mahjongColors
        val side = minOf(maxWidth, maxHeight)
        val rimShape = RoundedCornerShape(side * 0.07f)
        val feltShape = RoundedCornerShape(side * 0.045f)
        // Leaves room inside the felt for a lifted tile's ring.
        val feltPadding = (side * 0.035f).coerceAtLeast(12.dp)

        Box(
            modifier = Modifier
                .size(side)
                .shadow(8.dp, rimShape)
                .background(mahjongColors.feltRim, rimShape)
                .padding(side * 0.035f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(feltShape)
                    .drawWithCache {
                        val feltBrush = Brush.radialGradient(
                            colors = listOf(mahjongColors.feltLit, mahjongColors.felt),
                            center = size.center,
                            radius = size.minDimension * 0.72f,
                        )
                        onDrawBehind { drawRect(feltBrush) }
                    }
                    .border(1.dp, mahjongColors.inlay.copy(alpha = 0.7f), feltShape)
                    .padding(feltPadding),
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val inner = minOf(maxWidth, maxHeight)
                    val tileWidth = inner * TILE_WIDTH_FRACTION
                    val tileHeight = inner * TILE_HEIGHT_FRACTION

                    RoundCountHub(
                        roundsPlayed = roundsPlayed,
                        diameter = inner * HUB_FRACTION,
                        modifier = Modifier.align(Alignment.Center),
                    )

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
                            tileWidth = tileWidth,
                            onClick = { onSeatSelected(seat) },
                            modifier = Modifier
                                .align(alignment)
                                .size(width = tileWidth, height = tileHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundCountHub(
    roundsPlayed: Int,
    diameter: Dp,
    modifier: Modifier = Modifier,
) {
    val mahjongColors = MaterialTheme.mahjongColors
    val density = LocalDensity.current
    val countSize = with(density) { (diameter * 0.36f).toSp() }
    val labelSize = with(density) { (diameter * 0.13f).toSp() }

    Box(
        modifier = modifier
            .size(diameter)
            .background(mahjongColors.feltRim.copy(alpha = 0.32f), CircleShape)
            .border(1.dp, mahjongColors.inlay.copy(alpha = 0.55f), CircleShape)
            .semantics(mergeDescendants = true) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = roundsPlayed.toString(),
                style = ScoreNumerals,
                fontSize = countSize,
                color = mahjongColors.tileFace,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.table_rounds_label),
                fontSize = labelSize,
                fontWeight = FontWeight.Medium,
                color = mahjongColors.tileFace.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = diameter * 0.08f),
            )
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
            roundsPlayed = 3,
            highlightedWinner = Seat.EAST,
            onSeatSelected = {},
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
    }
}
