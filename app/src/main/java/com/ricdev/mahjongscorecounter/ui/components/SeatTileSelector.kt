package com.ricdev.mahjongscorecounter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors

private val TileSpacing = 12.dp
private val MaxTileWidth = 88.dp

/**
 * A row of wind tiles acting as a single-choice group. Tiles are always sized as if there were four,
 * so the winner row and the three-tile payer row line up.
 */
@Composable
fun SeatTileSelector(
    seats: List<Seat>,
    selected: Seat?,
    onSelectedChange: (Seat) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val tileWidth = ((maxWidth - TileSpacing * 3) / 4).coerceAtMost(MaxTileWidth)
        Row(
            modifier = Modifier
                // Room above for a lifted tile and its ring.
                .padding(top = 11.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(TileSpacing),
        ) {
            seats.forEach { seat ->
                val name = stringResource(seat.labelResId())
                val isSelected = seat == selected
                MahjongTile(
                    modifier = Modifier.size(width = tileWidth, height = 68.dp),
                    depth = 5.dp,
                    lifted = isSelected,
                    ringColor = MaterialTheme.colorScheme.primary,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    faceModifier = Modifier
                        .semantics { contentDescription = name }
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelectedChange(seat) },
                            role = Role.RadioButton,
                        ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val namedByGlyph = seat.isNamedByGlyph(name)
                        TileGlyph(
                            glyph = seat.windGlyph(),
                            fontSize = if (namedByGlyph) 30.sp else 24.sp,
                        )
                        if (!namedByGlyph) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.mahjongColors.tileInk.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, backgroundColor = 0xFFEEF2EC)
@Composable
private fun SeatTileSelectorPreview() {
    MahjongScoreCounterTheme {
        SeatTileSelector(
            seats = Seat.entries,
            selected = Seat.SOUTH,
            onSelectedChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
