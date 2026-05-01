package com.ricdev.mahjongscorecounter.ui.settings

import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ricdev.mahjongscorecounter.BuildConfig
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.adaptive.currentAdaptiveLayoutInfo
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
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    adaptiveLayoutInfo: AdaptiveLayoutInfo = currentAdaptiveLayoutInfo(),
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val versionName = BuildConfig.VERSION_NAME

    val currentTag = remember {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) "" else locales[0]?.toLanguageTag() ?: ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        if (adaptiveLayoutInfo.usesWideSettingsLayout) {
            WideSettingsContent(
                themeMode = themeMode,
                currentTag = currentTag,
                versionName = versionName,
                onThemeModeSelected = viewModel::updateThemeMode,
                onLocaleSelected = viewModel::updateLocale,
                modifier = Modifier
                    .widthIn(max = 1040.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .testTag("settings_content"),
            )
        } else {
            CompactSettingsContent(
                themeMode = themeMode,
                currentTag = currentTag,
                versionName = versionName,
                onThemeModeSelected = viewModel::updateThemeMode,
                onLocaleSelected = viewModel::updateLocale,
                modifier = Modifier
                    .then(
                        if (adaptiveLayoutInfo.constrainsSettingsWidth) {
                            Modifier.widthIn(max = 560.dp)
                        } else {
                            Modifier
                        }
                    )
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .testTag("settings_content"),
            )
        }
    }
}

@Composable
private fun CompactSettingsContent(
    themeMode: ThemeMode,
    currentTag: String,
    versionName: String,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLocaleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(stringResource(R.string.settings_appearance_header))
        AppearanceSettingsCard(
            themeMode = themeMode,
            onThemeModeSelected = onThemeModeSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionHeader(stringResource(R.string.settings_language_header))
        LanguageSettingsCard(
            currentTag = currentTag,
            onLocaleSelected = onLocaleSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionHeader(stringResource(R.string.settings_about_header))
        AboutSettingsCard(
            versionName = versionName,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WideSettingsContent(
    themeMode: ThemeMode,
    currentTag: String,
    versionName: String,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLocaleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeader(stringResource(R.string.settings_appearance_header))
                AppearanceSettingsCard(
                    themeMode = themeMode,
                    onThemeModeSelected = onThemeModeSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appearance_settings_card"),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeader(stringResource(R.string.settings_language_header))
                LanguageSettingsCard(
                    currentTag = currentTag,
                    onLocaleSelected = onLocaleSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("language_settings_card"),
                )
            }
        }

        SectionHeader(stringResource(R.string.settings_about_header))
        AboutSettingsCard(
            versionName = versionName,
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSettingsCard(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
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
                        onClick = { onThemeModeSelected(mode) },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSettingsCard(
    currentTag: String,
    onLocaleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        val selectedOption = languageOptions.firstOrNull { it.tag == currentTag }
            ?: languageOptions.first()

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = stringResource(selectedOption.labelResId),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_language_header)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                languageOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(option.labelResId)) },
                        onClick = {
                            expanded = false
                            onLocaleSelected(option.tag)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSettingsCard(
    versionName: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
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
                Column {
                    Text(stringResource(R.string.settings_about_version, versionName))
                    Text(stringResource(R.string.settings_about_scoring_model))
                }
            },
        )
    }
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
