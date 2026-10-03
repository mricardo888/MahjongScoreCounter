package com.ricdev.mahjongscorecounter.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.ui.adaptive.AdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.adaptive.currentAdaptiveLayoutInfo
import com.ricdev.mahjongscorecounter.ui.components.MahjongTile
import com.ricdev.mahjongscorecounter.ui.components.TileGlyph
import com.ricdev.mahjongscorecounter.ui.components.labelResId
import com.ricdev.mahjongscorecounter.ui.components.windGlyph
import com.ricdev.mahjongscorecounter.ui.theme.Bitter
import com.ricdev.mahjongscorecounter.viewmodel.FormState
import com.ricdev.mahjongscorecounter.viewmodel.GameViewModel
import com.ricdev.mahjongscorecounter.viewmodel.PreviewState
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
            roundsPlayed = gameState.history.size,
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
            roundsPlayed = gameState.history.size,
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

/**
 * Phone layout: nothing scrolls. The table fills the screen and rounds are entered in a sheet,
 * opened either from the "Record round" button or by tapping the winner's seat on the table.
 */
@Composable
private fun ScoreTrackerSinglePane(
    form: FormState,
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    roundsPlayed: Int,
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
    var showEntrySheet by rememberSaveable { mutableStateOf(false) }
    var focusAmountOnOpen by rememberSaveable { mutableStateOf(false) }

    val onSeatTapped: (Seat) -> Unit = { seat ->
        onWinnerSelected(seat)
        // The winner is already known, so the next thing to enter is the points.
        focusAmountOnOpen = true
        showEntrySheet = true
    }
    val onNewRound = {
        focusAmountOnOpen = false
        showEntrySheet = true
    }
    // The winner ring marks the round being entered; once it's recorded the table shows the result.
    val highlightedWinner = if (showEntrySheet) form.winner else null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("score_tracker_single_pane"),
    ) {
        if (maxWidth > maxHeight) {
            // Landscape phone: actions sit beside the table so the table keeps its height.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableScoreboard(
                    totals = totals,
                    lastRound = lastRound,
                    roundsPlayed = roundsPlayed,
                    highlightedWinner = highlightedWinner,
                    onSeatSelected = onSeatTapped,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Column(
                    modifier = Modifier.width(280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SeatTapHint(modifier = Modifier.fillMaxWidth())
                    TableActions(
                        canUndo = canUndo,
                        onUndo = onUndo,
                        onReset = onReset,
                        onNewRound = onNewRound,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table and hint stay together in the middle of whatever height is left.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TableScoreboard(
                        totals = totals,
                        lastRound = lastRound,
                        roundsPlayed = roundsPlayed,
                        highlightedWinner = highlightedWinner,
                        onSeatSelected = onSeatTapped,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .aspectRatio(1f),
                    )
                    SeatTapHint(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
                TableActions(
                    canUndo = canUndo,
                    onUndo = onUndo,
                    onReset = onReset,
                    onNewRound = onNewRound,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                )
            }
        }
    }

    if (showEntrySheet) {
        RoundEntrySheet(
            form = form,
            preview = preview,
            onWinnerSelected = onWinnerSelected,
            onWinTypeSelected = onWinTypeSelected,
            onAmountTextChange = onAmountTextChange,
            onPayerSelected = onPayerSelected,
            focusAmountOnOpen = focusAmountOnOpen,
            onRecord = onRecord,
            onDismiss = { showEntrySheet = false },
        )
    }
}

@Composable
private fun SeatTapHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.table_tap_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

/** One clear primary action, with the corrective actions kept quiet above it. */
@Composable
private fun TableActions(
    canUndo: Boolean,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onNewRound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.action_undo))
            }
            TextButton(
                onClick = onReset,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.action_reset))
            }
        }
        Button(
            onClick = onNewRound,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(
                text = stringResource(R.string.action_new_round),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundEntrySheet(
    form: FormState,
    preview: PreviewState,
    onWinnerSelected: (Seat) -> Unit,
    onWinTypeSelected: (WinType) -> Unit,
    onAmountTextChange: (String) -> Unit,
    onPayerSelected: (Seat?) -> Unit,
    focusAmountOnOpen: Boolean,
    onRecord: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val canRecord = preview is PreviewState.Valid

    val recordAndClose: () -> Unit = {
        if (canRecord) {
            onRecord()
            keyboard?.hide()
            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_new_round),
                    style = MaterialTheme.typography.titleLarge,
                )
                RoundEntryForm(
                    form = form,
                    onWinnerSelected = onWinnerSelected,
                    onWinTypeSelected = onWinTypeSelected,
                    onAmountTextChange = onAmountTextChange,
                    onPayerSelected = onPayerSelected,
                    amountFocusRequester = amountFocusRequester,
                    onAmountDone = {
                        // Done records when the round is complete; otherwise it reveals what's missing.
                        if (canRecord) recordAndClose() else keyboard?.hide()
                    },
                )
                CalculationSummary(
                    state = preview,
                    amountEntered = form.amountText.isNotBlank(),
                )
            }
            // Stays pinned above the keyboard while the form scrolls.
            Button(
                onClick = recordAndClose,
                enabled = canRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                    .heightIn(min = 56.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(R.string.action_commit_round),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (focusAmountOnOpen) {
            // Raise the keyboard only once the sheet has finished sliding up. Opening it mid-slide
            // makes the sheet re-anchor halfway through its animation, which reads as lag.
            snapshotFlow {
                sheetState.currentValue == SheetValue.Expanded && !sheetState.isAnimationRunning
            }.first { it }
            runCatching { amountFocusRequester.requestFocus() }
        }
    }
}

@Composable
private fun ScoreTrackerTwoPane(
    form: FormState,
    totals: Map<Seat, Int>,
    lastRound: CommittedRound?,
    roundsPlayed: Int,
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
            roundsPlayed = roundsPlayed,
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
    roundsPlayed: Int,
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
            roundsPlayed = roundsPlayed,
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
    val canRecord = preview is PreviewState.Valid

    Box(
        modifier = modifier.testTag("round_entry_pane"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(entryScrollState)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            RoundEntryForm(
                form = form,
                onWinnerSelected = onWinnerSelected,
                onWinTypeSelected = onWinTypeSelected,
                onAmountTextChange = onAmountTextChange,
                onPayerSelected = onPayerSelected,
                onAmountDone = { if (canRecord) onRecord() },
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
            )

            CalculationSummary(
                state = preview,
                amountEntered = form.amountText.isNotBlank(),
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
            )

            RoundActions(
                canRecord = canRecord,
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
    val targetAlpha = when {
        scrollState.maxValue <= 0 -> 0f
        scrollState.isScrollInProgress -> 0.6f
        else -> 0.25f
    }
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
                RecordButton(
                    enabled = canRecord,
                    onClick = onRecord,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UndoButton(
                        enabled = canUndo,
                        onClick = onUndo,
                        modifier = Modifier.weight(1f),
                    )
                    ResetButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RecordButton(
                    enabled = canRecord,
                    onClick = onRecord,
                    modifier = Modifier.weight(1.4f),
                )
                UndoButton(
                    enabled = canUndo,
                    onClick = onUndo,
                    modifier = Modifier.weight(1f),
                )
                ResetButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RecordButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(
            text = stringResource(R.string.action_commit_round),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun UndoButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Undo,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(R.string.action_undo))
    }
}

@Composable
private fun ResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.tertiary,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
    ) {
        Icon(
            imageVector = Icons.Rounded.RestartAlt,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(R.string.action_reset))
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
        if (isEmpty) {
            item {
                EmptyRounds(modifier = Modifier.fillParentMaxSize())
            }
        } else {
            roundsByDate.forEach { (dateLabel, rounds) ->
                item(key = "date-$dateLabel") {
                    DateHeader(dateLabel)
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isEmpty) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyRounds(modifier = Modifier.padding(top = 96.dp))
            }
        } else {
            roundsByDate.forEach { (dateLabel, rounds) ->
                item(
                    key = "date-$dateLabel",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    DateHeader(dateLabel)
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
private fun DateHeader(dateLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = Bitter),
            color = MaterialTheme.colorScheme.onSurface,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun EmptyRounds(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Seat.entries.forEach { seat ->
                MahjongTile(
                    modifier = Modifier.size(width = 40.dp, height = 52.dp),
                    depth = 5.dp,
                    cornerRadius = 7.dp,
                ) {
                    TileGlyph(glyph = seat.windGlyph(), fontSize = 24.sp)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.logs_empty),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.logs_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

@Composable
private fun RecentRoundCard(
    round: CommittedRound,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = remember { DateFormat.getTimeInstance(DateFormat.SHORT) }
    val numberFormatter = remember { NumberFormat.getInstance(Locale.getDefault()) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                MahjongTile(
                    modifier = Modifier.size(width = 34.dp, height = 42.dp),
                    depth = 4.dp,
                    cornerRadius = 6.dp,
                ) {
                    TileGlyph(glyph = round.input.winner.windGlyph(), fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
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
                    Text(
                        text = timeFormatter.format(Date(round.timestampMillis)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            DeltaStrip(
                deltas = round.result.deltas,
                winner = round.input.winner,
            )
        }
    }
}
