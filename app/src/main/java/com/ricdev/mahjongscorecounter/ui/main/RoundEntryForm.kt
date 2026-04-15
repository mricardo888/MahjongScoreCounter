package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.components.SegmentedButtonRow
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.viewmodel.FormState
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@Composable
fun RoundEntryForm(
    form: FormState,
    rules: ScoreRules,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onFanChange: (Int) -> Unit,
    onFlowerChange: (Int) -> Unit,
    onAnimalChange: (Int) -> Unit,
    onTaiChange: (Int) -> Unit,
    onHanChange: (Int) -> Unit,
    onFuChange: (Int) -> Unit,
    onDiscarderSelected: (Seat?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Shared: Winner ───────────────────────────────────────────────
        FieldLabel(text = stringResource(R.string.label_round_winner))
        SegmentedButtonRow(
            options = Seat.entries,
            selected = form.winner,
            onSelectedChange = onWinnerSelected,
            label = { seat -> stringResource(seat.shortLabelResId()) },
            modifier = Modifier.fillMaxWidth(),
        )

        // ── Shared: Win Type ─────────────────────────────────────────────
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

        // ── Variant-specific score inputs ────────────────────────────────
        when (form) {
            is FormState.Fan -> FanScoreInputs(
                form = form,
                rules = rules,
                onFanChange = onFanChange,
                onFlowerChange = onFlowerChange,
                onAnimalChange = onAnimalChange,
            )
            is FormState.Tai -> TaiScoreInputs(
                form = form,
                rules = rules,
                onTaiChange = onTaiChange,
            )
            is FormState.Riichi -> RiichiScoreInputs(
                form = form,
                onHanChange = onHanChange,
                onFuChange = onFuChange,
            )
        }

        // ── Shared: Discarder ────────────────────────────────────────────
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
private fun FanScoreInputs(
    form: FormState.Fan,
    rules: ScoreRules,
    onFanChange: (Int) -> Unit,
    onFlowerChange: (Int) -> Unit,
    onAnimalChange: (Int) -> Unit,
) {
    FieldLabel(text = stringResource(R.string.label_fan_count))
    CounterStepper(
        value = form.fanCount,
        onValueChange = onFanChange,
        min = GameViewModel.MIN_FAN,
        max = GameViewModel.MAX_FAN,
    )

    if (rules is ScoreRules.Singaporean) {
        FieldLabel(text = stringResource(R.string.label_flower_count))
        CounterStepper(
            value = form.flowerCount,
            onValueChange = onFlowerChange,
            min = 0,
            max = 8,
        )

        FieldLabel(text = stringResource(R.string.label_animal_count))
        CounterStepper(
            value = form.animalCount,
            onValueChange = onAnimalChange,
            min = 0,
            max = 4,
        )
    }
}

@Composable
private fun TaiScoreInputs(
    form: FormState.Tai,
    rules: ScoreRules,
    onTaiChange: (Int) -> Unit,
) {
    val minTai = when (rules) {
        is ScoreRules.Taiwanese -> rules.minTai
        else -> GameViewModel.MIN_TAI
    }
    FieldLabel(text = stringResource(R.string.label_tai_count))
    CounterStepper(
        value = form.taiCount,
        onValueChange = onTaiChange,
        min = minTai,
        max = GameViewModel.MAX_TAI,
    )
}

@Composable
private fun RiichiScoreInputs(
    form: FormState.Riichi,
    onHanChange: (Int) -> Unit,
    onFuChange: (Int) -> Unit,
) {
    FieldLabel(text = stringResource(R.string.label_han))
    CounterStepper(
        value = form.han,
        onValueChange = onHanChange,
        min = GameViewModel.MIN_HAN,
        max = GameViewModel.MAX_HAN,
    )

    FieldLabel(text = stringResource(R.string.label_fu))
    FuSelector(
        fu = form.fu,
        onFuSelected = onFuChange,
    )
}

@Composable
private fun FuSelector(fu: Int, onFuSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GameViewModel.FU_OPTIONS.forEach { option ->
            FilterChip(
                selected = fu == option,
                onClick = { onFuSelected(option) },
                label = { Text(option.toString(), style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun CounterStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
    ) {
        FilledIconButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(min)) },
            enabled = value > min,
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 8.dp),
        )
        FilledIconButton(
            onClick = { onValueChange((value + 1).coerceAtMost(max)) },
            enabled = value < max,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
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
private fun RoundEntryFormFanPreview() {
    MahjongScoreCounterTheme {
        RoundEntryForm(
            form = FormState.Fan(
                winner = Seat.EAST,
                winType = WinType.DISCARD_WIN,
                fanCount = 3,
                discarder = Seat.SOUTH,
            ),
            rules = ScoreRules.HongKongNew(),
            onWinnerSelected = {},
            onWinTypeSelected = {},
            onFanChange = {},
            onFlowerChange = {},
            onAnimalChange = {},
            onTaiChange = {},
            onHanChange = {},
            onFuChange = {},
            onDiscarderSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RoundEntryFormRiichiPreview() {
    MahjongScoreCounterTheme {
        RoundEntryForm(
            form = FormState.Riichi(
                winner = Seat.WEST,
                winType = WinType.SELF_DRAW,
                han = 3,
                fu = 30,
            ),
            rules = ScoreRules.JapaneseRiichi(),
            onWinnerSelected = {},
            onWinTypeSelected = {},
            onFanChange = {},
            onFlowerChange = {},
            onAnimalChange = {},
            onTaiChange = {},
            onHanChange = {},
            onFuChange = {},
            onDiscarderSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
