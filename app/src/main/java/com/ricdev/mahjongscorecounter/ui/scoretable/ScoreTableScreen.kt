package com.ricdev.mahjongscorecounter.ui.scoretable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@Composable
fun ScoreTableScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val rules = gameState.rules

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel(stringResource(R.string.score_table_rules_header))

        when (rules) {
            is ScoreRules.HongKongNew -> HongKongNewEditor(rules = rules, onSave = viewModel::updateRules)
            is ScoreRules.Singaporean -> SingaporeanEditor(rules = rules, onSave = viewModel::updateRules)
            is ScoreRules.Taiwanese -> TaiwaneseEditor(rules = rules, onSave = viewModel::updateRules)
            is ScoreRules.Hokkien -> HokkienEditor(rules = rules, onSave = viewModel::updateRules)
            is ScoreRules.Shanghai -> ShanghaiEditor(rules = rules, onSave = viewModel::updateRules)
            is ScoreRules.Sichuan -> SichuanEditor(rules = rules, onSave = viewModel::updateRules)
            is ScoreRules.JapaneseRiichi -> RiichiEditor(rules = rules, onSave = viewModel::updateRules)
        }
    }
}

// ─── HK New / Singaporean (linear fan) ───────────────────────────────────────

@Composable
private fun HongKongNewEditor(rules: ScoreRules.HongKongNew, onSave: (ScoreRules) -> Unit) {
    var discardText by remember(rules.discardBase) { mutableStateOf(rules.discardBase.toString()) }
    var selfDrawText by remember(rules.selfDrawBase) { mutableStateOf(rules.selfDrawBase.toString()) }
    var isCustomSelfDraw by remember(rules) { mutableStateOf(rules.selfDrawBase != rules.discardBase / 2) }
    var maxFanText by remember(rules.maxFan) { mutableStateOf((rules.maxFan ?: 0).toString()) }

    LaunchedEffect(rules) {
        discardText = rules.discardBase.toString()
        selfDrawText = rules.selfDrawBase.toString()
        isCustomSelfDraw = rules.selfDrawBase != rules.discardBase / 2
        maxFanText = (rules.maxFan ?: 0).toString()
    }

    val discardValue = discardText.toIntOrNull()
    val derivedSelfDraw = discardValue?.div(2) ?: (rules.discardBase / 2)
    val selfDrawValue = if (isCustomSelfDraw) selfDrawText.toIntOrNull() else derivedSelfDraw
    val maxFanValue = maxFanText.toIntOrNull()
    val isValid = discardValue != null && discardValue >= 0 &&
        selfDrawValue != null && selfDrawValue >= 0 &&
        maxFanValue != null && maxFanValue >= 0

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = discardText,
                onValueChange = { discardText = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.rules_hk_discard_base)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.rules_hk_custom_self_draw_title), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.rules_hk_custom_self_draw_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isCustomSelfDraw,
                    onCheckedChange = { checked ->
                        isCustomSelfDraw = checked
                        if (!checked) selfDrawText = derivedSelfDraw.toString()
                    },
                    modifier = Modifier.padding(start = 16.dp),
                )
            }

            if (isCustomSelfDraw) {
                OutlinedTextField(
                    value = selfDrawText,
                    onValueChange = { selfDrawText = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.rules_hk_self_draw_base)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(stringResource(R.string.rules_hk_self_draw_derived, derivedSelfDraw), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            OutlinedTextField(
                value = maxFanText,
                onValueChange = { maxFanText = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.rules_hk_max_fan)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    Button(
        onClick = {
            if (isValid) {
                onSave(ScoreRules.HongKongNew(
                    discardBase = discardValue!!,
                    selfDrawBase = selfDrawValue!!,
                    maxFan = if (maxFanValue == 0) null else maxFanValue,
                ))
            }
        },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    // Reference table
    SectionLabel(stringResource(R.string.score_table_fan_points_title))
    val effDiscard = discardValue ?: rules.discardBase
    val effSelfDraw = selfDrawValue ?: rules.selfDrawBase
    val maxFan = (maxFanValue?.takeIf { it > 0 }) ?: 10
    FanReferenceTable(
        fanRange = 1..minOf(maxFan, 10),
        selfDrawForFan = { fan -> effSelfDraw * fan },
        discardForFan = { fan -> effDiscard * fan },
    )
}

@Composable
private fun SingaporeanEditor(rules: ScoreRules.Singaporean, onSave: (ScoreRules) -> Unit) {
    var discardText by remember(rules.discardBase) { mutableStateOf(rules.discardBase.toString()) }
    var selfDrawText by remember(rules.selfDrawBase) { mutableStateOf(rules.selfDrawBase.toString()) }
    var flowerText by remember(rules.flowerBonus) { mutableStateOf(rules.flowerBonus.toString()) }
    var animalText by remember(rules.animalBonus) { mutableStateOf(rules.animalBonus.toString()) }

    val discardValue = discardText.toIntOrNull()
    val selfDrawValue = selfDrawText.toIntOrNull()
    val flowerValue = flowerText.toIntOrNull()
    val animalValue = animalText.toIntOrNull()
    val isValid = listOf(discardValue, selfDrawValue, flowerValue, animalValue).all { it != null && it >= 0 }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IntField(discardText, { discardText = it }, stringResource(R.string.rules_hk_discard_base))
            IntField(selfDrawText, { selfDrawText = it }, stringResource(R.string.rules_hk_self_draw_base))
            IntField(flowerText, { flowerText = it }, stringResource(R.string.rules_sg_flower_bonus))
            IntField(animalText, { animalText = it }, stringResource(R.string.rules_sg_animal_bonus))
        }
    }

    Button(
        onClick = {
            if (isValid) {
                onSave(ScoreRules.Singaporean(discardBase = discardValue!!, selfDrawBase = selfDrawValue!!, flowerBonus = flowerValue!!, animalBonus = animalValue!!))
            }
        },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    SectionLabel(stringResource(R.string.score_table_fan_points_title))
    FanReferenceTable(
        fanRange = 1..10,
        selfDrawForFan = { fan -> (selfDrawValue ?: rules.selfDrawBase) * fan },
        discardForFan = { fan -> (discardValue ?: rules.discardBase) * fan },
    )
}

// ─── Shanghai / Sichuan (doubling fan) ───────────────────────────────────────

@Composable
private fun ShanghaiEditor(rules: ScoreRules.Shanghai, onSave: (ScoreRules) -> Unit) {
    var baseText by remember(rules.base) { mutableStateOf(rules.base.toString()) }
    var maxFanText by remember(rules.maxFan) { mutableStateOf(rules.maxFan.toString()) }

    val baseValue = baseText.toIntOrNull()
    val maxFanValue = maxFanText.toIntOrNull()
    val isValid = baseValue != null && baseValue >= 1 && maxFanValue != null && maxFanValue >= 1

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IntField(baseText, { baseText = it }, stringResource(R.string.rules_sh_base))
            IntField(maxFanText, { maxFanText = it }, stringResource(R.string.rules_sh_max_fan))
        }
    }

    Button(
        onClick = { if (isValid) onSave(ScoreRules.Shanghai(base = baseValue!!, maxFan = maxFanValue!!)) },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    SectionLabel(stringResource(R.string.score_table_fan_points_title))
    val effBase = baseValue ?: rules.base
    val effMax = maxFanValue ?: rules.maxFan
    DoublingFanTable(base = effBase, maxFan = effMax)
}

@Composable
private fun SichuanEditor(rules: ScoreRules.Sichuan, onSave: (ScoreRules) -> Unit) {
    var baseText by remember(rules.base) { mutableStateOf(rules.base.toString()) }
    var maxFanText by remember(rules.maxFan) { mutableStateOf(rules.maxFan.toString()) }

    val baseValue = baseText.toIntOrNull()
    val maxFanValue = maxFanText.toIntOrNull()
    val isValid = baseValue != null && baseValue >= 1 && maxFanValue != null && maxFanValue >= 1

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IntField(baseText, { baseText = it }, stringResource(R.string.rules_sh_base))
            IntField(maxFanText, { maxFanText = it }, stringResource(R.string.rules_sh_max_fan))
        }
    }

    Button(
        onClick = { if (isValid) onSave(ScoreRules.Sichuan(base = baseValue!!, maxFan = maxFanValue!!)) },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    SectionLabel(stringResource(R.string.score_table_fan_points_title))
    DoublingFanTable(base = baseValue ?: rules.base, maxFan = maxFanValue ?: rules.maxFan)
}

// ─── Taiwanese ───────────────────────────────────────────────────────────────

@Composable
private fun TaiwaneseEditor(rules: ScoreRules.Taiwanese, onSave: (ScoreRules) -> Unit) {
    var baseText by remember(rules.base) { mutableStateOf(rules.base.toString()) }
    var perTaiText by remember(rules.perTai) { mutableStateOf(rules.perTai.toString()) }
    var minTaiText by remember(rules.minTai) { mutableStateOf(rules.minTai.toString()) }
    var bonusTaiText by remember(rules.selfDrawBonusTai) { mutableStateOf(rules.selfDrawBonusTai.toString()) }
    var dealerMultText by remember(rules.dealerMultiplier) { mutableStateOf(rules.dealerMultiplier.toString()) }

    val baseV = baseText.toIntOrNull()
    val perTaiV = perTaiText.toIntOrNull()
    val minTaiV = minTaiText.toIntOrNull()
    val bonusTaiV = bonusTaiText.toIntOrNull()
    val dealerMultV = dealerMultText.toIntOrNull()
    val isValid = listOf(baseV, perTaiV, minTaiV, bonusTaiV, dealerMultV).all { it != null && it >= 0 }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IntField(baseText, { baseText = it }, stringResource(R.string.rules_tw_base))
            IntField(perTaiText, { perTaiText = it }, stringResource(R.string.rules_tw_per_tai))
            IntField(minTaiText, { minTaiText = it }, stringResource(R.string.rules_tw_min_tai))
            IntField(bonusTaiText, { bonusTaiText = it }, stringResource(R.string.rules_tw_self_draw_bonus_tai))
            IntField(dealerMultText, { dealerMultText = it }, stringResource(R.string.rules_tw_dealer_multiplier))
        }
    }

    Button(
        onClick = {
            if (isValid) onSave(ScoreRules.Taiwanese(base = baseV!!, perTai = perTaiV!!, minTai = minTaiV!!, selfDrawBonusTai = bonusTaiV!!, dealerMultiplier = dealerMultV!!))
        },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    SectionLabel(stringResource(R.string.score_table_fan_points_title))
    val effBase = baseV ?: rules.base
    val effPerTai = perTaiV ?: rules.perTai
    TaiReferenceTable(
        taiRange = (minTaiV ?: rules.minTai)..(minTaiV?.plus(7) ?: (rules.minTai + 7)),
        payoutForTai = { tai -> effBase + tai * effPerTai },
    )
}

// ─── Hokkien ─────────────────────────────────────────────────────────────────

@Composable
private fun HokkienEditor(rules: ScoreRules.Hokkien, onSave: (ScoreRules) -> Unit) {
    var baseText by remember(rules.base) { mutableStateOf(rules.base.toString()) }
    var perUnitText by remember(rules.perUnit) { mutableStateOf(rules.perUnit.toString()) }
    var maxUnitsText by remember(rules.maxUnits) { mutableStateOf((rules.maxUnits ?: 0).toString()) }
    var dealerDoubles by remember(rules.dealerDoubles) { mutableStateOf(rules.dealerDoubles) }

    val baseV = baseText.toIntOrNull()
    val perUnitV = perUnitText.toIntOrNull()
    val maxUnitsV = maxUnitsText.toIntOrNull()
    val isValid = baseV != null && baseV >= 0 && perUnitV != null && perUnitV >= 0 && maxUnitsV != null && maxUnitsV >= 0

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IntField(baseText, { baseText = it }, stringResource(R.string.rules_hok_base))
            IntField(perUnitText, { perUnitText = it }, stringResource(R.string.rules_hok_per_unit))
            IntField(maxUnitsText, { maxUnitsText = it }, stringResource(R.string.rules_hok_max_units))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.rules_hok_dealer_doubles), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = dealerDoubles, onCheckedChange = { dealerDoubles = it })
            }
        }
    }

    Button(
        onClick = {
            if (isValid) onSave(ScoreRules.Hokkien(base = baseV!!, perUnit = perUnitV!!, maxUnits = if (maxUnitsV == 0) null else maxUnitsV, dealerDoubles = dealerDoubles))
        },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    SectionLabel(stringResource(R.string.score_table_fan_points_title))
    val effBase = baseV ?: rules.base
    val effPerUnit = perUnitV ?: rules.perUnit
    val effMax = (maxUnitsV?.takeIf { it > 0 }) ?: (rules.maxUnits) ?: 10
    TaiReferenceTable(
        taiRange = 1..minOf(effMax, 10),
        payoutForTai = { tai -> effBase + tai * effPerUnit },
    )
}

// ─── Japanese Riichi ──────────────────────────────────────────────────────────

@Composable
private fun RiichiEditor(rules: ScoreRules.JapaneseRiichi, onSave: (ScoreRules) -> Unit) {
    var honbaText by remember(rules.honbaValue) { mutableStateOf(rules.honbaValue.toString()) }
    var stickText by remember(rules.riichiStickValue) { mutableStateOf(rules.riichiStickValue.toString()) }
    var kiriageMangan by remember(rules.kiriageMangan) { mutableStateOf(rules.kiriageMangan) }

    val honbaV = honbaText.toIntOrNull()
    val stickV = stickText.toIntOrNull()
    val isValid = honbaV != null && honbaV >= 0 && stickV != null && stickV >= 0

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.rules_ri_kiriage_mangan), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = kiriageMangan, onCheckedChange = { kiriageMangan = it })
            }
            IntField(honbaText, { honbaText = it }, stringResource(R.string.rules_ri_honba_value))
            IntField(stickText, { stickText = it }, stringResource(R.string.rules_ri_stick_value))
        }
    }

    Button(
        onClick = {
            if (isValid) onSave(ScoreRules.JapaneseRiichi(kiriageMangan = kiriageMangan, honbaValue = honbaV!!, riichiStickValue = stickV!!))
        },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_save)) }

    SectionLabel(stringResource(R.string.score_table_riichi_reference))
    RiichiTierTable()
}

// ─── Shared reference tables ──────────────────────────────────────────────────

@Composable
private fun FanReferenceTable(
    fanRange: IntRange,
    selfDrawForFan: (Int) -> Int,
    discardForFan: (Int) -> Int,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TableCell(stringResource(R.string.score_table_col_fan), Modifier.weight(0.6f), FontWeight.SemiBold, TextAlign.Start)
                TableCell(stringResource(R.string.score_table_col_self_draw), Modifier.weight(1f), FontWeight.SemiBold)
                TableCell(stringResource(R.string.score_table_col_discard), Modifier.weight(1f), FontWeight.SemiBold)
            }
            fanRange.forEachIndexed { index, fan ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TableCell(fan.toString(), Modifier.weight(0.6f), textAlign = TextAlign.Start)
                    TableCell(selfDrawForFan(fan).toString(), Modifier.weight(1f))
                    TableCell(discardForFan(fan).toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DoublingFanTable(base: Int, maxFan: Int) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TableCell(stringResource(R.string.score_table_col_fan), Modifier.weight(0.6f), FontWeight.SemiBold, TextAlign.Start)
                TableCell(stringResource(R.string.score_table_col_payout), Modifier.weight(1f), FontWeight.SemiBold)
            }
            (1..minOf(maxFan, 10)).forEachIndexed { index, fan ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val payout = base * (1 shl (fan - 1))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TableCell(fan.toString(), Modifier.weight(0.6f), textAlign = TextAlign.Start)
                    TableCell(payout.toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TaiReferenceTable(taiRange: IntRange, payoutForTai: (Int) -> Int) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TableCell(stringResource(R.string.score_table_col_tai), Modifier.weight(0.6f), FontWeight.SemiBold, TextAlign.Start)
                TableCell(stringResource(R.string.score_table_col_payout), Modifier.weight(1f), FontWeight.SemiBold)
            }
            taiRange.forEachIndexed { index, tai ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TableCell(tai.toString(), Modifier.weight(0.6f), textAlign = TextAlign.Start)
                    TableCell(payoutForTai(tai).toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

private data class RiichiTierRow(val name: String, val nonDealerRon: String, val dealerRon: String, val nonDealerTsumo: String, val dealerTsumo: String)

@Composable
private fun RiichiTierTable() {
    val tiers = listOf(
        RiichiTierRow("Mangan", "8,000", "12,000", "2,000 / 4,000", "4,000 all"),
        RiichiTierRow("Haneman", "12,000", "18,000", "3,000 / 6,000", "6,000 all"),
        RiichiTierRow("Baiman", "16,000", "24,000", "4,000 / 8,000", "8,000 all"),
        RiichiTierRow("Sanbaiman", "24,000", "36,000", "6,000 / 12,000", "12,000 all"),
        RiichiTierRow("Yakuman", "32,000", "48,000", "8,000 / 16,000", "16,000 all"),
    )

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TableCell("Tier", Modifier.weight(1.2f), FontWeight.SemiBold, TextAlign.Start)
                TableCell(stringResource(R.string.score_table_col_non_dealer_ron), Modifier.weight(1f), FontWeight.SemiBold)
                TableCell(stringResource(R.string.score_table_col_dealer_ron), Modifier.weight(1f), FontWeight.SemiBold)
            }
            tiers.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TableCell(row.name, Modifier.weight(1.2f), textAlign = TextAlign.Start)
                    TableCell(row.nonDealerRon, Modifier.weight(1f))
                    TableCell(row.dealerRon, Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun IntField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
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
        style = MaterialTheme.typography.bodySmall,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}
