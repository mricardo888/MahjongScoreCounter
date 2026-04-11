package com.ricdev.mahjongscorecounter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.ui.logs.LogsScreen
import com.ricdev.mahjongscorecounter.ui.main.MainScreen
import com.ricdev.mahjongscorecounter.ui.nav.Destinations
import com.ricdev.mahjongscorecounter.ui.settings.SettingsScreen
import com.ricdev.mahjongscorecounter.ui.stats.StatsScreen
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahjongApp() {
    val context = LocalContext.current.applicationContext
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(context))
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val title = when (currentRoute) {
        Destinations.LOGS -> stringResource(R.string.title_logs)
        Destinations.STATS -> stringResource(R.string.title_stats)
        Destinations.SETTINGS -> stringResource(R.string.title_settings)
        else -> stringResource(R.string.title_main)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    if (currentRoute != Destinations.SETTINGS) {
                        IconButton(onClick = {
                            navController.navigate(Destinations.SETTINGS) {
                                launchSingleTop = true
                            }
                        }) {
                            Text(
                                text = stringResource(R.string.action_settings),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        bottomBar = {
            if (currentRoute != Destinations.SETTINGS) {
                BottomAppBar(
                    actions = {
                        TextButton(onClick = {
                            navController.navigate(Destinations.LOGS) {
                                launchSingleTop = true
                            }
                        }) {
                            Text(stringResource(R.string.nav_logs))
                        }
                        TextButton(onClick = {
                            navController.navigate(Destinations.STATS) {
                                launchSingleTop = true
                            }
                        }) {
                            Text(stringResource(R.string.nav_stats))
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            navController.navigate(Destinations.MAIN) {
                                launchSingleTop = true
                                popUpTo(Destinations.MAIN) { inclusive = false }
                            }
                        }) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    },
                )
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
