package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.components.SegmentedButtonRow
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.viewmodel.FormState

@Composable
fun RoundEntryForm(
    form: FormState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onAmountChange: (Int) -> Unit,
    onPayerSelected: (Seat?) -> Unit,
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

        AmountField(
            amount = form.amount,
            labelResId = when (form.winType) {
                WinType.SELF_DRAW -> R.string.label_amount_self_draw
                WinType.DISCARD_WIN -> R.string.label_amount_discard
            },
            onAmountChange = onAmountChange,
        )

        if (form.winType == WinType.DISCARD_WIN && form.winner != null) {
            FieldLabel(text = stringResource(R.string.label_payer))
            val payerOptions = Seat.entries.filter { it != form.winner }
            SegmentedButtonRow(
                options = payerOptions,
                selected = form.payer,
                onSelectedChange = onPayerSelected,
                label = { seat -> stringResource(seat.shortLabelResId()) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AmountField(
    amount: Int,
    labelResId: Int,
    onAmountChange: (Int) -> Unit,
) {
    var amountText by remember { mutableStateOf(amount.takeIf { it > 0 }?.toString().orEmpty()) }

    LaunchedEffect(amount) {
        amountText = amount.takeIf { it > 0 }?.toString().orEmpty()
    }

    OutlinedTextField(
        value = amountText,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() }
            amountText = filtered
            onAmountChange(filtered.toIntOrNull() ?: 0)
        },
        label = { Text(stringResource(labelResId)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

internal fun Seat.shortLabelResId(): Int = when (this) {
    Seat.EAST -> R.string.seat_east_short
    Seat.SOUTH -> R.string.seat_south_short
    Seat.WEST -> R.string.seat_west_short
    Seat.NORTH -> R.string.seat_north_short
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RoundEntryFormPreview() {
    MahjongScoreCounterTheme {
        RoundEntryForm(
            form = FormState(
                winner = Seat.WEST,
                winType = WinType.DISCARD_WIN,
                payer = Seat.NORTH,
                amount = 8,
            ),
            onWinnerSelected = {},
            onWinTypeSelected = {},
            onAmountChange = {},
            onPayerSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
