package com.ricdev.mahjongscorecounter.ui.main

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.data.GameRepository
import com.ricdev.mahjongscorecounter.logic.ScoreEngine
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.MahjongApp
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveHeightSizeClass
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveWidthSizeClass
import com.ricdev.mahjongscorecounter.ui.settings.SettingsScreen
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class MainScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun recordUndoAndResetUpdateVisibleScores() {
        val repository = FakeGameRepository()
        val viewModel = GameViewModel(repository)

        composeRule.setContent {
            MahjongScoreCounterTheme {
                MainScreen(
                    viewModel = viewModel,
                    contentPadding = PaddingValues(0.dp),
                    adaptiveLayoutInfo = compactLayoutInfo(),
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(seatScoreDescription(Seat.EAST, "0"), substring = true)
            .performClick()
        composeRule
            .onNodeWithTag("round_amount")
            .performScrollTo()
            .performTextInput("3")
        composeRule
            .onNodeWithText(context.getString(R.string.action_commit_round))
            .assertIsEnabled()
            .performClick()

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(seatScoreDescription(Seat.EAST, "9"), substring = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.action_undo))
            .performClick()
        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(seatScoreDescription(Seat.EAST, "0"), substring = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.action_reset))
            .performClick()
        composeRule
            .onNodeWithText(context.getString(R.string.reset_dialog_confirm))
            .performClick()
        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(seatScoreDescription(Seat.EAST, "0"), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun recentRoundsShowsAllPersistedRoundsNewestFirst() {
        val repository = FakeGameRepository()
        repository.setHistory(
            (1..6).map { amount ->
                val input = RoundInput(
                    winner = Seat.EAST,
                    winType = WinType.SELF_DRAW,
                    amount = amount,
                )
                CommittedRound(
                    input = input,
                    result = ScoreEngine.calculate(input),
                    timestampMillis = amount.toLong() * 60_000L,
                )
            },
        )
        val viewModel = GameViewModel(repository)

        composeRule.setContent {
            MahjongScoreCounterTheme {
                RecentRoundsScreen(
                    viewModel = viewModel,
                    contentPadding = PaddingValues(0.dp),
                    adaptiveLayoutInfo = compactLayoutInfo(),
                )
            }
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.logs_entry_self_draw,
                    context.getString(R.string.seat_east),
                    "6",
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.logs_entry_self_draw,
                    context.getString(R.string.seat_east),
                    "1",
                ),
            )
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun compactAppUsesBottomNavigationAndSingleColumnScoring() {
        val viewModel = GameViewModel(FakeGameRepository())

        composeRule.setContent {
            MahjongScoreCounterTheme {
                MahjongApp(
                    viewModel = viewModel,
                    adaptiveLayoutInfo = compactLayoutInfo(),
                )
            }
        }

        composeRule.onNodeWithTag("bottom_navigation").assertIsDisplayed()
        composeRule.onNodeWithTag("score_tracker_single_pane").assertIsDisplayed()
    }

    @Test
    fun expandedAppUsesNavigationRailAndTwoScorePanes() {
        val viewModel = GameViewModel(FakeGameRepository())

        composeRule.setContent {
            MahjongScoreCounterTheme {
                MahjongApp(
                    viewModel = viewModel,
                    adaptiveLayoutInfo = expandedLayoutInfo(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("score_tracker_two_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("scoreboard_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("round_entry_pane").assertIsDisplayed()
    }

    @Test
    fun expandedRecentRoundsUsesGridAndKeepsNewestFirstOrder() {
        val repository = FakeGameRepository()
        repository.setHistory(
            (1..6).map { amount ->
                val input = RoundInput(
                    winner = Seat.EAST,
                    winType = WinType.SELF_DRAW,
                    amount = amount,
                )
                CommittedRound(
                    input = input,
                    result = ScoreEngine.calculate(input),
                    timestampMillis = amount.toLong() * 60_000L,
                )
            },
        )
        val viewModel = GameViewModel(repository)

        composeRule.setContent {
            MahjongScoreCounterTheme {
                Box(modifier = Modifier.requiredSize(width = 900.dp, height = 700.dp)) {
                    RecentRoundsScreen(
                        viewModel = viewModel,
                        contentPadding = PaddingValues(0.dp),
                        adaptiveLayoutInfo = expandedLayoutInfo(),
                    )
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("recent_rounds_grid").assertIsDisplayed()
        val newestBounds = composeRule
            .onNodeWithText(roundTitle(6))
            .getUnclippedBoundsInRoot()
        val nextBounds = composeRule
            .onNodeWithText(roundTitle(5))
            .getUnclippedBoundsInRoot()
        val oldestBounds = composeRule
            .onNodeWithText(roundTitle(1))
            .getUnclippedBoundsInRoot()

        assertTrue(abs((newestBounds.top - nextBounds.top).value) < 1f)
        assertTrue(nextBounds.left > newestBounds.left)
        assertTrue(oldestBounds.top > newestBounds.top)
        assertTrue(composeRule.onAllNodesWithTag("recent_round_card").fetchSemanticsNodes().size >= 6)
    }

    @Test
    fun expandedSettingsConstrainsControlsOnTabletWidth() {
        val viewModel = GameViewModel(FakeGameRepository())

        composeRule.setContent {
            MahjongScoreCounterTheme {
                Box(modifier = Modifier.requiredSize(width = 1280.dp, height = 800.dp)) {
                    SettingsScreen(
                        viewModel = viewModel,
                        contentPadding = PaddingValues(0.dp),
                        adaptiveLayoutInfo = AdaptiveLayoutInfo(
                            widthSizeClass = AdaptiveWidthSizeClass.Large,
                            heightSizeClass = AdaptiveHeightSizeClass.Medium,
                        ),
                    )
                }
            }
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithText(context.getString(R.string.settings_theme_label))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.settings_language_header))
            .assertIsDisplayed()

        val contentBounds = composeRule
            .onNodeWithTag("settings_content")
            .getUnclippedBoundsInRoot()
        val languageCardBounds = composeRule
            .onNodeWithTag("language_settings_card")
            .getUnclippedBoundsInRoot()
        val contentWidth = contentBounds.right - contentBounds.left
        val languageCardWidth = languageCardBounds.right - languageCardBounds.left

        assertTrue(contentWidth <= 1040.dp)
        assertTrue(languageCardWidth <= 560.dp)
    }

    private fun seatScoreDescription(seat: Seat, total: String): String {
        val seatLabel = context.getString(
            when (seat) {
                Seat.EAST -> R.string.seat_east
                Seat.SOUTH -> R.string.seat_south
                Seat.WEST -> R.string.seat_west
                Seat.NORTH -> R.string.seat_north
            },
        )
        return context.getString(R.string.accessibility_seat_score, seatLabel, total)
    }

    private fun roundTitle(amount: Int): String = context.getString(
        R.string.logs_entry_self_draw,
        context.getString(R.string.seat_east),
        amount.toString(),
    )

    private fun compactLayoutInfo(): AdaptiveLayoutInfo = AdaptiveLayoutInfo(
        widthSizeClass = AdaptiveWidthSizeClass.Compact,
        heightSizeClass = AdaptiveHeightSizeClass.Medium,
    )

    private fun expandedLayoutInfo(): AdaptiveLayoutInfo = AdaptiveLayoutInfo(
        widthSizeClass = AdaptiveWidthSizeClass.Expanded,
        heightSizeClass = AdaptiveHeightSizeClass.Medium,
    )
}

private class FakeGameRepository : GameRepository(dataStore = StubDataStore) {
    private val history = MutableStateFlow<List<CommittedRound>>(emptyList())
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    override val historyFlow: Flow<List<CommittedRound>> = history
    override val themeModeFlow: Flow<ThemeMode> = themeMode

    fun setHistory(value: List<CommittedRound>) {
        history.value = value
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    override suspend fun appendRound(round: CommittedRound) {
        history.value = history.value + round
    }

    override suspend fun dropLastRound() {
        val current = history.value
        if (current.isNotEmpty()) {
            history.value = current.dropLast(1)
        }
    }

    override suspend fun clearHistory() {
        history.value = emptyList()
    }
}

private val StubDataStore = object : DataStore<Preferences> {
    override val data: Flow<Preferences>
        get() = emptyFlow()

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        throw UnsupportedOperationException("Fake repository should not touch the data store")
    }
}
