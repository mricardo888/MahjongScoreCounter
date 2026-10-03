package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.components.SeatTileSelector
import com.ricdev.mahjongscorecounter.ui.components.SegmentedButtonRow
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.ScoreNumerals
import com.ricdev.mahjongscorecounter.viewmodel.FormState

@Composable
fun RoundEntryForm(
    form: FormState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onAmountTextChange: (String) -> Unit,
    onPayerSelected: (Seat?) -> Unit,
    modifier: Modifier = Modifier,
    amountFocusRequester: FocusRequester? = null,
    onAmountDone: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        FormField(label = stringResource(R.string.label_round_winner)) {
            SeatTileSelector(
                seats = Seat.entries,
                selected = form.winner,
                onSelectedChange = onWinnerSelected,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FormField(label = stringResource(R.string.label_win_type)) {
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
        }

        if (form.winType == WinType.DISCARD_WIN && form.winner != null) {
            FormField(label = stringResource(R.string.label_payer)) {
                SeatTileSelector(
                    seats = Seat.entries.filter { it != form.winner },
                    selected = form.payer,
                    onSelectedChange = onPayerSelected,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AmountField(
            amountText = form.amountText,
            labelResId = when (form.winType) {
                WinType.SELF_DRAW -> R.string.label_amount_self_draw
                WinType.DISCARD_WIN -> R.string.label_amount_discard
            },
            onAmountTextChange = onAmountTextChange,
            focusRequester = amountFocusRequester,
            onDone = onAmountDone,
        )
    }
}

@Composable
private fun FormField(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(text = label)
        content()
    }
}

@Composable
private fun AmountField(
    amountText: String,
    labelResId: Int,
    onAmountTextChange: (String) -> Unit,
    focusRequester: FocusRequester?,
    onDone: (() -> Unit)?,
) {
    OutlinedTextField(
        value = amountText,
        onValueChange = { raw ->
            onAmountTextChange(raw)
        },
        label = { Text(stringResource(labelResId)) },
        textStyle = ScoreNumerals.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .testTag("round_amount"),
    )
}

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true, widthDp = 360, backgroundColor = 0xFFEEF2EC)
@Composable
private fun RoundEntryFormPreview() {
    MahjongScoreCounterTheme {
        RoundEntryForm(
            form = FormState(
                winner = Seat.WEST,
                winType = WinType.DISCARD_WIN,
                payer = Seat.NORTH,
                amountText = "8",
            ),
            onWinnerSelected = {},
            onWinTypeSelected = {},
            onAmountTextChange = {},
            onPayerSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
