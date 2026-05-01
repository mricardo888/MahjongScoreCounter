package com.ricdev.mahjongscorecounter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.adaptive.currentAdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.main.MainScreen
import com.ricdev.mahjongscorecounter.ui.main.RecentRoundsScreen
import com.ricdev.mahjongscorecounter.ui.settings.SettingsScreen
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahjongApp(
    viewModel: GameViewModel,
    adaptiveLayoutInfo: AdaptiveLayoutInfo = currentAdaptiveLayoutInfo(),
) {
    val tabs = AppTab.entries
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val navigationSuiteType = if (adaptiveLayoutInfo.usesNavigationRail) {
        NavigationSuiteType.NavigationRail
    } else {
        NavigationSuiteType.NavigationBar
    }

    NavigationSuiteScaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(
                if (adaptiveLayoutInfo.usesNavigationRail) {
                    "navigation_rail"
                } else {
                    "bottom_navigation"
                }
            ),
        layoutType = navigationSuiteType,
        navigationSuiteItems = {
            tabs.forEachIndexed { index, tab ->
                item(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    icon = {
                        val label = stringResource(tab.titleResId)
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = label,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.titleResId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                )
            },
        ) { innerPadding ->
            when (tabs[selectedTabIndex]) {
                AppTab.SCORE_TRACKER -> {
                    MainScreen(
                        viewModel = viewModel,
                        contentPadding = innerPadding,
                        adaptiveLayoutInfo = adaptiveLayoutInfo,
                    )
                }
                AppTab.RECENT_ROUNDS -> {
                    RecentRoundsScreen(
                        viewModel = viewModel,
                        contentPadding = innerPadding,
                        adaptiveLayoutInfo = adaptiveLayoutInfo,
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        contentPadding = innerPadding,
                        adaptiveLayoutInfo = adaptiveLayoutInfo,
                    )
                }
            }
        }
    }
}

private enum class AppTab(
    val titleResId: Int,
    val icon: ImageVector,
) {
    SCORE_TRACKER(R.string.title_main, Icons.Rounded.Calculate),
    RECENT_ROUNDS(R.string.recent_rounds_title, Icons.Rounded.History),
    SETTINGS(R.string.title_settings, Icons.Rounded.Settings),
}
