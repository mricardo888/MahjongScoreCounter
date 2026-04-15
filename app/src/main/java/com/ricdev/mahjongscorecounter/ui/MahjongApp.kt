package com.ricdev.mahjongscorecounter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.ui.logs.LogsScreen
import com.ricdev.mahjongscorecounter.ui.main.MainScreen
import com.ricdev.mahjongscorecounter.ui.nav.Destinations
import com.ricdev.mahjongscorecounter.ui.scoretable.ScoreTableScreen
import com.ricdev.mahjongscorecounter.ui.settings.SettingsScreen
import com.ricdev.mahjongscorecounter.ui.stats.StatsScreen
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahjongApp(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isSettings = currentRoute == Destinations.SETTINGS

    val title = when (currentRoute) {
        Destinations.SCORE_TABLE -> stringResource(R.string.title_score_table)
        Destinations.LOGS -> stringResource(R.string.title_logs)
        Destinations.STATS -> stringResource(R.string.title_stats)
        Destinations.SETTINGS -> stringResource(R.string.title_settings)
        else -> stringResource(R.string.title_main)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (isSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                actions = {
                    if (!isSettings) {
                        IconButton(onClick = {
                            navController.navigate(Destinations.SETTINGS) {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.action_settings),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!isSettings) {
                NavigationBar {
                    val isMain = currentRoute == Destinations.MAIN || currentRoute == null
                    val isScoreTable = currentRoute == Destinations.SCORE_TABLE
                    val isLogs = currentRoute == Destinations.LOGS
                    val isStats = currentRoute == Destinations.STATS

                    NavigationBarItem(
                        selected = isMain,
                        onClick = {
                            navController.navigate(Destinations.MAIN) {
                                launchSingleTop = true
                                popUpTo(Destinations.MAIN) { inclusive = false }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isMain) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.nav_scoreboard)) },
                    )
                    NavigationBarItem(
                        selected = isScoreTable,
                        onClick = {
                            navController.navigate(Destinations.SCORE_TABLE) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isScoreTable) Icons.Filled.TableChart else Icons.Outlined.TableChart,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.nav_score_table)) },
                    )
                    NavigationBarItem(
                        selected = isLogs,
                        onClick = {
                            navController.navigate(Destinations.LOGS) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isLogs) {
                                    Icons.AutoMirrored.Filled.List
                                } else {
                                    Icons.AutoMirrored.Outlined.List
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.nav_logs)) },
                    )
                    NavigationBarItem(
                        selected = isStats,
                        onClick = {
                            navController.navigate(Destinations.STATS) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isStats) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.nav_stats)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.MAIN,
        ) {
            composable(Destinations.MAIN) {
                MainScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Destinations.SCORE_TABLE) {
                ScoreTableScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Destinations.LOGS) {
                LogsScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Destinations.STATS) {
                StatsScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    contentPadding = innerPadding,
                )
            }
        }
    }
}
