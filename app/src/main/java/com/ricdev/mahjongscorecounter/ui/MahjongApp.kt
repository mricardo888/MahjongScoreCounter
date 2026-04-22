package com.ricdev.mahjongscorecounter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.ricdev.mahjongscorecounter.ui.main.MainScreen
import com.ricdev.mahjongscorecounter.ui.nav.Destinations
import com.ricdev.mahjongscorecounter.ui.settings.SettingsScreen
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahjongApp(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isSettings = currentRoute == Destinations.SETTINGS

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isSettings) R.string.title_settings else R.string.title_main
                        )
                    )
                },
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.MAIN,
        ) {
            composable(Destinations.MAIN) {
                MainScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}
