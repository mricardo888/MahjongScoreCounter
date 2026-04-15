package com.ricdev.mahjongscorecounter.ui.settings

import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.LocaleListCompat
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.MahjongVariant
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

private data class LanguageOption(val tag: String, val labelResId: Int)

private val languageOptions = listOf(
    LanguageOption("", R.string.settings_language_system),
    LanguageOption("en", R.string.language_english),
    LanguageOption("ko", R.string.language_korean),
    LanguageOption("vi", R.string.language_vietnamese),
    LanguageOption("ja", R.string.language_japanese),
    LanguageOption("fil", R.string.language_filipino),
    LanguageOption("zh-HK", R.string.language_zh_hk),
    LanguageOption("zh-TW", R.string.language_zh_tw),
    LanguageOption("zh-CN", R.string.language_zh_cn),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val currentVariant = gameState.variant
    var pendingVariant by remember { mutableStateOf<MahjongVariant?>(null) }
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "\u2014"
        }.getOrElse { "\u2014" }
    }

    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) {
        ""
    } else {
        currentLocales[0]?.toLanguageTag() ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Game Variant ─────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.settings_variant_header))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                MahjongVariant.entries.forEach { variant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (variant != currentVariant) {
                                    pendingVariant = variant
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = currentVariant == variant, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(variant.labelResId()),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        // ── Appearance ───────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.settings_appearance_header))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { viewModel.updateThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ThemeMode.entries.size,
                            ),
                        ) {
                            Text(
                                text = stringResource(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> R.string.settings_theme_system
                                        ThemeMode.LIGHT -> R.string.settings_theme_light
                                        ThemeMode.DARK -> R.string.settings_theme_dark
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }

        // ── Language ─────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.settings_language_header))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                languageOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val locales = if (option.tag.isEmpty()) {
                                    LocaleListCompat.getEmptyLocaleList()
                                } else {
                                    LocaleListCompat.forLanguageTags(option.tag)
                                }
                                AppCompatDelegate.setApplicationLocales(locales)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currentTag == option.tag,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(option.labelResId),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        // ── About ─────────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.settings_about_header))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                leadingContent = {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                setImageResource(R.mipmap.ic_launcher_round)
                            }
                        },
                        modifier = Modifier.size(48.dp),
                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    Text(stringResource(R.string.settings_about_version, versionName))
                },
            )
        }
    }

    if (pendingVariant != null) {
        AlertDialog(
            onDismissRequest = { pendingVariant = null },
            title = { Text(stringResource(R.string.settings_variant_change_title)) },
            text = { Text(stringResource(R.string.settings_variant_change_message)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingVariant?.let { viewModel.changeVariant(it) }
                    pendingVariant = null
                }) {
                    Text(stringResource(R.string.settings_variant_change_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingVariant = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

internal fun MahjongVariant.labelResId(): Int = when (this) {
    MahjongVariant.HONG_KONG_NEW -> R.string.variant_hong_kong_new
    MahjongVariant.TAIWANESE -> R.string.variant_taiwanese
    MahjongVariant.JAPANESE_RIICHI -> R.string.variant_japanese_riichi
    MahjongVariant.HOKKIEN -> R.string.variant_hokkien
    MahjongVariant.SHANGHAI -> R.string.variant_shanghai
    MahjongVariant.SICHUAN -> R.string.variant_sichuan
    MahjongVariant.SINGAPOREAN -> R.string.variant_singaporean
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}
