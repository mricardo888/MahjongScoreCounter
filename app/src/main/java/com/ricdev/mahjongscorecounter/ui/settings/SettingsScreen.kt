package com.ricdev.mahjongscorecounter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.logic.ScoreEngine
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val rules = gameState.rules

    var selfDrawText by remember(rules.selfDrawBase) { mutableStateOf(rules.selfDrawBase.toString()) }
    var discardText by remember(rules.discardWinBase) { mutableStateOf(rules.discardWinBase.toString()) }

    LaunchedEffect(rules) {
        selfDrawText = rules.selfDrawBase.toString()
        discardText = rules.discardWinBase.toString()
    }

    val selfDrawValue = selfDrawText.toIntOrNull()
    val discardValue = discardText.toIntOrNull()
    val isValid = selfDrawValue != null && discardValue != null &&
        selfDrawValue >= 0 && discardValue >= 0
    val previewRules = ScoreRules(
        selfDrawBase = selfDrawValue ?: rules.selfDrawBase,
        discardWinBase = discardValue ?: rules.discardWinBase,
    )
    val tableRows = remember(previewRules) {
        (GameViewModel.MIN_FAN..GameViewModel.MAX_FAN).map { fan ->
            FanPointsRow(
                fan = fan,
                selfDrawPoints = ScoreEngine.amountForFan(previewRules.selfDrawBase, fan),
                discardPoints = ScoreEngine.amountForFan(previewRules.discardWinBase, fan),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_rules_header),
            style = MaterialTheme.typography.titleLarge,
        )

        OutlinedTextField(
            value = selfDrawText,
            onValueChange = { selfDrawText = it.filter(Char::isDigit) },
            label = { Text(stringResource(R.string.settings_self_draw_base)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = discardText,
            onValueChange = { discardText = it.filter(Char::isDigit) },
            label = { Text(stringResource(R.string.settings_discard_win_base)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        if (!isValid) {
            Text(
                text = stringResource(R.string.error_negative_base),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }

        FanPointsTable(rows = tableRows)

        Button(
            onClick = {
                if (isValid) {
                    viewModel.updateRules(
                        ScoreRules(
                            selfDrawBase = selfDrawValue!!,
                            discardWinBase = discardValue!!,
                        )
                    )
                    onBack()
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

private data class FanPointsRow(
    val fan: Int,
    val selfDrawPoints: Int,
    val discardPoints: Int,
)

@Composable
private fun FanPointsTable(rows: List<FanPointsRow>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_fan_points_table_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TableCell(
                        text = stringResource(R.string.settings_fan_points_column_fan),
                        modifier = Modifier.weight(0.8f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start,
                    )
                    TableCell(
                        text = stringResource(R.string.settings_fan_points_column_self_draw),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    TableCell(
                        text = stringResource(R.string.settings_fan_points_column_discard),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                rows.forEachIndexed { index, row ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TableCell(
                            text = row.fan.toString(),
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Start,
                        )
                        TableCell(
                            text = row.selfDrawPoints.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        TableCell(
                            text = row.discardPoints.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.End,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}
