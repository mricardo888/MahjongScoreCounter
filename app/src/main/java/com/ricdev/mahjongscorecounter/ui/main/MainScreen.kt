package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.adaptive.currentAdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors
import com.ricdev.mahjongscorecounter.viewmodel.FormState
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel
import com.ricdev.mahjongscorecounter.viewmodel.PreviewState
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun MainScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    adaptiveLayoutInfo: AdaptiveLayoutInfo = currentAdaptiveLayoutInfo(),
) {
    val form by viewModel.formState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val preview by viewModel.preview.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    if (adaptiveLayoutInfo.usesTwoPaneScoreTracker) {
        ScoreTrackerTwoPane(
            form = form,
            totals = gameState.totals,
            lastRound = gameState.history.lastOrNull(),
            preview = preview,
            onWinnerSelected = viewModel::selectWinner,
            onWinTypeSelected = viewModel::selectWinType,
            onAmountTextChange = viewModel::setAmountText,
            onPayerSelected = viewModel::selectPayer,
            canUndo = gameState.history.isNotEmpty(),
            onRecord = viewModel::commitRound,
            onUndo = viewModel::undoLast,
            onReset = { showResetDialog = true },
            modifier = modifier.padding(contentPadding),
        )
    } else {
        ScoreTrackerSinglePane(
            form = form,
            totals = gameState.totals,
            lastRound = gameState.history.lastOrNull(),
            preview = preview,
            onWinnerSelected = viewModel::selectWinner,
            onWinTypeSelected = viewModel::selectWinType,
            onAmountTextChange = viewModel::setAmountText,
            onPayerSelected = viewModel::selectPayer,
            canUndo = gameState.history.isNotEmpty(),
            onRecord = viewModel::commitRound,
            onUndo = viewModel::undoLast,
            onReset = { showResetDialog = true },
            modifier = modifier.padding(contentPadding),
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.resetGame()
                }) {
                    Text(stringResource(R.string.reset_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun ScoreTrackerSinglePane(
    form: FormState,
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    preview: PreviewState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onAmountTextChange: (String) -> Unit,
    onPayerSelected: (Seat?) -> Unit,
    canUndo: Boolean,
    onRecord: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("score_tracker_single_pane"),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(mainScrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TableScoreboard(
                    totals = totals,
                    lastRound = lastRound,
                    highlightedWinner = form.winner,
                    onSeatSelected = onWinnerSelected,
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally),
                )

                RoundEntryForm(
                    form = form,
                    onWinnerSelected = onWinnerSelected,
                    onWinTypeSelected = onWinTypeSelected,
                    onAmountTextChange = onAmountTextChange,
                    onPayerSelected = onPayerSelected,
                )

                CalculationSummary(state = preview)
            }

            EdgeScrollBar(
                scrollState = mainScrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            )
        }

        RoundActions(
            canRecord = preview is PreviewState.Valid,
            canUndo = canUndo,
            onRecord = onRecord,
            onUndo = onUndo,
            onReset = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun ScoreTrackerTwoPane(
    form: FormState,
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    preview: PreviewState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onAmountTextChange: (String) -> Unit,
    onPayerSelected: (Seat?) -> Unit,
    canUndo: Boolean,
    onRecord: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("score_tracker_two_pane"),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScoreboardPane(
            totals = totals,
            lastRound = lastRound,
            highlightedWinner = form.winner,
            onSeatSelected = onWinnerSelected,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )

        RoundEntryPane(
            form = form,
            preview = preview,
            onWinnerSelected = onWinnerSelected,
            onWinTypeSelected = onWinTypeSelected,
            onAmountTextChange = onAmountTextChange,
            onPayerSelected = onPayerSelected,
            canUndo = canUndo,
            onRecord = onRecord,
            onUndo = onUndo,
            onReset = onReset,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun ScoreboardPane(
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    highlightedWinner: Seat?,
    onSeatSelected: (Seat) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.testTag("scoreboard_pane"),
        contentAlignment = Alignment.Center,
    ) {
        val side = minOf(maxWidth, maxHeight, 600.dp)
        TableScoreboard(
            totals = totals,
            lastRound = lastRound,
            highlightedWinner = highlightedWinner,
            onSeatSelected = onSeatSelected,
            modifier = Modifier.size(side),
        )
    }
}

@Composable
private fun RoundEntryPane(
    form: FormState,
    preview: PreviewState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onAmountTextChange: (String) -> Unit,
    onPayerSelected: (Seat?) -> Unit,
    canUndo: Boolean,
    onRecord: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entryScrollState = rememberScrollState()

    Box(
        modifier = modifier.testTag("round_entry_pane"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(entryScrollState)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            RoundEntryForm(
                form = form,
                onWinnerSelected = onWinnerSelected,
                onWinTypeSelected = onWinTypeSelected,
                onAmountTextChange = onAmountTextChange,
                onPayerSelected = onPayerSelected,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
            )

            CalculationSummary(
                state = preview,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
            )

            RoundActions(
                canRecord = preview is PreviewState.Valid,
                canUndo = canUndo,
                onRecord = onRecord,
                onUndo = onUndo,
                onReset = onReset,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
            )
        }

        EdgeScrollBar(
            scrollState = entryScrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun EdgeScrollBar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val targetAlpha = if (scrollState.maxValue > 0 && scrollState.isScrollInProgress) 0.6f else 0f
    val alpha by animateFloatAsState(targetValue = targetAlpha, label = "main_scrollbar_alpha")

    if (alpha <= 0.01f) return

    val density = LocalDensity.current
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
    val minThumbHeight = with(density) { 32.dp.toPx() }
    val thumbWidth = with(density) { 4.dp.toPx() }

    Canvas(modifier = modifier.width(8.dp)) {
        val scrollRange = scrollState.maxValue.toFloat()
        val viewportHeight = size.height
        if (scrollRange <= 0f || viewportHeight <= 0f) return@Canvas

        val contentHeight = viewportHeight + scrollRange
        val thumbHeight = max(
            minThumbHeight,
            viewportHeight * viewportHeight / contentHeight,
        ).coerceAtMost(viewportHeight)
        val thumbTop = scrollState.value / scrollRange * (viewportHeight - thumbHeight)

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(size.width - thumbWidth, thumbTop),
            size = Size(thumbWidth, thumbHeight),
            cornerRadius = CornerRadius(thumbWidth / 2f, thumbWidth / 2f),
        )
    }
}

@Composable
private fun RoundActions(
    canRecord: Boolean,
    canUndo: Boolean,
    onRecord: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 380.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRecord,
                    enabled = canRecord,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_commit_round))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_undo))
                    }
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_reset))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onRecord,
                    enabled = canRecord,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_commit_round))
                }
                OutlinedButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_undo))
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_reset))
                }
            }
        }
    }
}

@Composable
fun RecentRoundsScreen(
    viewModel: GameViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    adaptiveLayoutInfo: AdaptiveLayoutInfo = currentAdaptiveLayoutInfo(),
) {
    val gameState by viewModel.gameState.collectAsState()
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }
    val roundsByDate = remember(gameState.history) {
        gameState.history
            .asReversed()
            .groupBy { round -> dateFormatter.format(Date(round.timestampMillis)) }
    }

    if (adaptiveLayoutInfo.usesRecentRoundsGrid) {
        RecentRoundsGrid(
            roundsByDate = roundsByDate,
            isEmpty = gameState.history.isEmpty(),
            contentPadding = contentPadding,
            modifier = modifier,
        )
    } else {
        RecentRoundsList(
            roundsByDate = roundsByDate,
            isEmpty = gameState.history.isEmpty(),
            contentPadding = contentPadding,
            modifier = modifier,
        )
    }
}

@Composable
private fun RecentRoundsList(
    roundsByDate: Map<String, List<CommittedRound>>,
    isEmpty: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("recent_rounds_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.recent_rounds_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (isEmpty) {
            item {
                Text(
                    text = stringResource(R.string.logs_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            roundsByDate.forEach { (dateLabel, rounds) ->
                item(key = "date-$dateLabel") {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(items = rounds) { round ->
                    RecentRoundCard(
                        round = round,
                        modifier = Modifier.testTag("recent_round_card"),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRoundsGrid(
    roundsByDate: Map<String, List<CommittedRound>>,
    isEmpty: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 360.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("recent_rounds_grid"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.recent_rounds_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (isEmpty) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.logs_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            roundsByDate.forEach { (dateLabel, rounds) ->
                item(
                    key = "date-$dateLabel",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                gridItems(items = rounds) { round ->
                    RecentRoundCard(
                        round = round,
                        modifier = Modifier.testTag("recent_round_card"),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRoundCard(
    round: CommittedRound,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = remember { DateFormat.getTimeInstance(DateFormat.SHORT) }
    val numberFormatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = timeFormatter.format(Date(round.timestampMillis)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val winnerLabel = stringResource(round.input.winner.labelResId())
            val amountLabel = numberFormatter.format(round.input.amount)
            val title = when (round.input.winType) {
                WinType.SELF_DRAW -> stringResource(
                    R.string.logs_entry_self_draw,
                    winnerLabel,
                    amountLabel,
                )
                WinType.DISCARD_WIN -> {
                    val payerLabel = stringResource(
                        (round.input.payer ?: round.input.winner).labelResId()
                    )
                    stringResource(
                        R.string.logs_entry_discard,
                        winnerLabel,
                        payerLabel,
                        amountLabel,
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Seat.entries.chunked(2).forEach { rowSeats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowSeats.forEach { seat ->
                            val delta = round.result.deltas[seat] ?: 0
                            DeltaChip(
                                seat = seat,
                                delta = delta,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeltaChip(
    seat: Seat,
    delta: Int,
    modifier: Modifier = Modifier,
) {
    val mahjongColors = MaterialTheme.mahjongColors
    val formatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    val color: Color = when {
        delta > 0 -> mahjongColors.deltaPositive
        delta < 0 -> mahjongColors.deltaNegative
        else -> mahjongColors.deltaNeutral
    }
    val sign = if (delta > 0) "+" else ""
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = "${stringResource(seat.labelResId())} $sign${formatter.format(delta)}",
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}
