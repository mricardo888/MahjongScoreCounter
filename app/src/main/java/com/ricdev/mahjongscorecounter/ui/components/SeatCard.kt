package com.ricdev.mahjongscorecounter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.ScoreNumerals
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors
import java.text.NumberFormat
import java.util.Locale

/**
 * A seat on the table, drawn as a wind tile. Type is sized from [tileWidth] rather than the user's
 * font scale: the table is a fixed-proportion board that already scales with the window.
 */
@Composable
fun SeatCard(
    seat: Seat,
    total: Int,
    lastDelta: Int?,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    tileWidth: Dp = 112.dp,
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

    val density = LocalDensity.current
    fun Dp.asSp() = with(density) { toSp() }
    val glyphSize = (tileWidth * 0.2f).asSp()
    val nameSize = (tileWidth * 0.105f).asSp()
    val scoreMax = (tileWidth * 0.25f).asSp()
    val scoreMin = (tileWidth * 0.12f).asSp()
    val deltaSize = (tileWidth * 0.115f).asSp()

    MahjongTile(
        modifier = modifier,
        depth = (tileWidth * 0.055f).coerceIn(4.dp, 9.dp),
        cornerRadius = (tileWidth * 0.1f).coerceIn(8.dp, 16.dp),
        lifted = highlighted,
        faceModifier = Modifier
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
        contentPadding = PaddingValues(
            horizontal = tileWidth * 0.08f,
            vertical = tileWidth * 0.06f,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tileWidth * 0.05f),
            ) {
                TileGlyph(glyph = seat.windGlyph(), fontSize = glyphSize)
                if (!seat.isNamedByGlyph(seatLabel)) {
                    Text(
                        text = seatLabel,
                        color = mahjongColors.tileInk.copy(alpha = 0.7f),
                        fontSize = nameSize,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            BasicText(
                text = formatter.formatScore(total),
                modifier = Modifier.fillMaxWidth(),
                style = ScoreNumerals.copy(
                    color = mahjongColors.tileInk,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = scoreMin,
                    maxFontSize = scoreMax,
                ),
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val deltaColor = when {
                    lastDelta == null || lastDelta == 0 -> mahjongColors.tileInk
                    lastDelta > 0 -> mahjongColors.tileDeltaPositive
                    else -> mahjongColors.tileDeltaNegative
                }
                Text(
                    // Always laid out so every tile keeps the same rhythm, scored or not.
                    text = lastDelta?.takeIf { it != 0 }?.let { formatter.formatDelta(it) } ?: " ",
                    color = deltaColor,
                    style = ScoreNumerals.copy(fontWeight = FontWeight.SemiBold),
                    fontSize = deltaSize,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF145C42)
@Composable
private fun SeatCardHighlightedPreview() {
    MahjongScoreCounterTheme {
        SeatCard(
            seat = Seat.EAST,
            total = 768,
            lastDelta = 256,
            highlighted = true,
            modifier = Modifier
                .padding(16.dp)
                .size(width = 112.dp, height = 100.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF145C42)
@Composable
private fun SeatCardIdlePreview() {
    MahjongScoreCounterTheme {
        SeatCard(
            seat = Seat.SOUTH,
            total = -256,
            lastDelta = -256,
            highlighted = false,
            modifier = Modifier
                .padding(16.dp)
                .size(width = 112.dp, height = 100.dp),
        )
    }
}
