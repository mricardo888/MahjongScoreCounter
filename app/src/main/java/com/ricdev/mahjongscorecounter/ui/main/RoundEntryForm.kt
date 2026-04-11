package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.components.SegmentedButtonRow
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.viewmodel.FormState
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@Composable
fun RoundEntryForm(
    form: FormState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onFanChange: (Int) -> Unit,
    onDiscarderSelected: (Seat?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FieldLabel(text = stringResource(R.string.label_round_winner))
        SegmentedButtonRow(
            options = Seat.entries,
            selected = form.winner,
            onSelectedChange = onWinnerSelected,
            label = { seat -> stringResource(seat.shortLabelResId()) },
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel(text = stringResource(R.string.label_win_type))
        SegmentedButtonRow(
            options = WinType.entries,
            selected = form.winType,
            onSelectedChange = onWinTypeSelected,
            label = { type ->
                stringResource(
                    when (type) {
                        WinType.SELF_DRAW -> R.string.win_type_self_draw
                        WinType.DISCARD_WIN -> R.string.win_type_discard
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel(text = stringResource(R.string.label_fan_count))
        FanStepper(
            fan = form.fanCount,
            onFanChange = onFanChange,
        )

        if (form.winType == WinType.DISCARD_WIN && form.winner != null) {
            FieldLabel(text = stringResource(R.string.label_discarder))
            val discarderOptions = Seat.entries.filter { it != form.winner }
            SegmentedButtonRow(
                options = discarderOptions,
                selected = form.discarder,
                onSelectedChange = onDiscarderSelected,
                label = { seat -> stringResource(seat.shortLabelResId()) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FanStepper(
    fan: Int,
    onFanChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FilledIconButton(
            onClick = { onFanChange((fan - 1).coerceAtLeast(GameViewModel.MIN_FAN)) },
            enabled = fan > GameViewModel.MIN_FAN,
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            text = fan.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 8.dp),
        )
        FilledIconButton(
            onClick = { onFanChange((fan + 1).coerceAtMost(GameViewModel.MAX_FAN)) },
            enabled = fan < GameViewModel.MAX_FAN,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

private fun Seat.shortLabelResId(): Int = when (this) {
    Seat.EAST -> R.string.seat_east_short
    Seat.SOUTH -> R.string.seat_south_short
    Seat.WEST -> R.string.seat_west_short
    Seat.NORTH -> R.string.seat_north_short
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RoundEntryFormDiscardPreview() {
    MahjongScoreCounterTheme {
        RoundEntryForm(
            form = FormState(
                winner = Seat.EAST,
                winType = WinType.DISCARD_WIN,
                fanCount = 3,
                discarder = Seat.SOUTH,
            ),
            onWinnerSelected = {},
            onWinTypeSelected = {},
            onFanChange = {},
            onDiscarderSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RoundEntryFormSelfDrawPreview() {
    MahjongScoreCounterTheme {
        RoundEntryForm(
            form = FormState(
                winner = Seat.WEST,
                winType = WinType.SELF_DRAW,
                fanCount = 7,
            ),
            onWinnerSelected = {},
            onWinTypeSelected = {},
            onFanChange = {},
            onDiscarderSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
