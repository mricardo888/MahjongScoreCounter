package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.ui.components.formatDelta
import com.ricdev.mahjongscorecounter.ui.components.labelResId
import com.ricdev.mahjongscorecounter.ui.components.shortLabelResId
import com.ricdev.mahjongscorecounter.ui.theme.ScoreNumerals
import java.text.NumberFormat
import java.util.Locale

/** One scoresheet row: every seat's change for a round, in seat order, winner marked. */
@Composable
internal fun DeltaStrip(
    deltas: Map<Seat, Int>,
    winner: Seat,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Seat.entries.forEach { seat ->
            val delta = deltas[seat] ?: 0
            val seatName = stringResource(seat.labelResId())
            val deltaLabel = formatter.formatDelta(delta)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (seat == winner) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(7.dp),
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 6.dp)
                    .clearAndSetSemantics { contentDescription = "$seatName $deltaLabel" },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(seat.shortLabelResId()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = deltaLabel,
                    style = ScoreNumerals.copy(fontWeight = FontWeight.SemiBold),
                    fontSize = 17.sp,
                    color = deltaColor(delta),
                    maxLines = 1,
                )
            }
        }
    }
}
