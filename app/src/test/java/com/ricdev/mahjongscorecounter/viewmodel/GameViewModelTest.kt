package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.data.GameRepository
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.MahjongVariant
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: FakeGameRepository
    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeGameRepository()
        viewModel = GameViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ─── HK New (Fan) ────────────────────────────────────────────────────────

    @Test
    fun `commit then undo round-trips totals and history`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setFan(3)
        advanceUntilIdle()

        viewModel.commitRound()
        advanceUntilIdle()

        val afterCommit = viewModel.gameState.first()
        assertEquals(1, afterCommit.history.size)
        // HKNew defaults: selfDrawBase=1, discardBase=2; fan=3 → perLoser = 1*3 = 3, EAST gains 3*3 = 9
        assertEquals(9, afterCommit.totals[Seat.EAST])
        assertEquals(-3, afterCommit.totals[Seat.SOUTH])

        viewModel.undoLast()
        advanceUntilIdle()

        val afterUndo = viewModel.gameState.first()
        assertEquals(0, afterUndo.history.size)
        Seat.entries.forEach { seat ->
            assertEquals(0, afterUndo.totals[seat])
        }
    }

    @Test
    fun `selectWinType SELF_DRAW clears previously set discarder`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectDiscarder(Seat.SOUTH)
        assertEquals(Seat.SOUTH, viewModel.formState.first().discarder)

        viewModel.selectWinType(WinType.SELF_DRAW)
        assertNull(viewModel.formState.first().discarder)
    }

    @Test
    fun `commitRound with valid form resets fan and clears discarder, keeping winner`() =
        runTest {
            viewModel.selectWinner(Seat.WEST)
            viewModel.selectWinType(WinType.DISCARD_WIN)
            viewModel.selectDiscarder(Seat.NORTH)
            viewModel.setFan(5)
            advanceUntilIdle()

            viewModel.commitRound()
            advanceUntilIdle()

            val form = viewModel.formState.first() as FormState.Fan
            assertEquals(Seat.WEST, form.winner)
            assertEquals(1, form.fanCount)
            assertNull(form.discarder)

            val state = viewModel.gameState.first()
            assertEquals(1, state.history.size)
        }

    @Test
    fun `commitRound with invalid preview is a no-op`() = runTest {
        // no winner selected → preview is Empty
        viewModel.setFan(3)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
        val state = viewModel.gameState.first()
        assertEquals(0, state.history.size)
    }

    @Test
    fun `setFan clamps outside 1-20`() = runTest {
        viewModel.setFan(0)
        assertEquals(1, (viewModel.formState.first() as FormState.Fan).fanCount)
        viewModel.setFan(50)
        assertEquals(20, (viewModel.formState.first() as FormState.Fan).fanCount)
    }

    @Test
    fun `preview becomes Valid when winner selected for self draw`() = runTest {
        assertTrue(viewModel.preview.first() is PreviewState.Empty)
        viewModel.selectWinner(Seat.EAST)
        advanceUntilIdle()
        assertTrue(viewModel.preview.first() is PreviewState.Valid)
    }

    @Test
    fun `preview is Invalid when discard win missing discarder`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        advanceUntilIdle()
        val preview = viewModel.preview.first()
        assertTrue(preview is PreviewState.Invalid)
    }

    @Test
    fun `resetGame clears history and form`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.SELF_DRAW)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
        assertFalse(viewModel.gameState.first().history.isEmpty())

        viewModel.resetGame()
        advanceUntilIdle()

        assertEquals(0, viewModel.gameState.first().history.size)
        assertNull(viewModel.formState.first().winner)
    }

    // ─── Variant switching ────────────────────────────────────────────────────

    @Test
    fun `changeVariant to Taiwanese switches FormState to Tai`() = runTest {
        viewModel.changeVariant(MahjongVariant.TAIWANESE)
        advanceUntilIdle()

        assertTrue(viewModel.formState.first() is FormState.Tai)
        val rules = viewModel.currentRules()
        assertTrue(rules is ScoreRules.Taiwanese)
    }

    @Test
    fun `changeVariant to JapaneseRiichi switches FormState to Riichi`() = runTest {
        viewModel.changeVariant(MahjongVariant.JAPANESE_RIICHI)
        advanceUntilIdle()

        assertTrue(viewModel.formState.first() is FormState.Riichi)
        val rules = viewModel.currentRules()
        assertTrue(rules is ScoreRules.JapaneseRiichi)
    }

    @Test
    fun `changeVariant clears history`() = runTest {
        // commit a round first
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.SELF_DRAW)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
        assertEquals(1, viewModel.gameState.first().history.size)

        viewModel.changeVariant(MahjongVariant.TAIWANESE)
        advanceUntilIdle()

        assertEquals(0, viewModel.gameState.first().history.size)
    }

    @Test
    fun `changeVariant to same variant is a no-op`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.SELF_DRAW)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
        assertEquals(1, viewModel.gameState.first().history.size)

        // changing to same variant should not clear history
        viewModel.changeVariant(MahjongVariant.HONG_KONG_NEW)
        advanceUntilIdle()

        assertEquals(1, viewModel.gameState.first().history.size)
    }

    // ─── Dealer rotation ─────────────────────────────────────────────────────

    @Test
    fun `non-dealer Fan win does not rotate dealer`() = runTest {
        // FAN variant has no dealer rotation
        assertEquals(Seat.EAST, viewModel.gameState.first().dealer)
        viewModel.selectWinner(Seat.SOUTH) // non-dealer wins
        viewModel.selectWinType(WinType.SELF_DRAW)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()

        assertEquals(Seat.EAST, viewModel.gameState.first().dealer)
    }

    @Test
    fun `non-dealer Tai win rotates dealer`() = runTest {
        viewModel.changeVariant(MahjongVariant.TAIWANESE)
        advanceUntilIdle()

        assertEquals(Seat.EAST, viewModel.gameState.first().dealer)

        viewModel.selectWinner(Seat.SOUTH) // non-dealer (SOUTH) wins
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setTai(5)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()

        assertEquals(Seat.SOUTH, viewModel.gameState.first().dealer)
    }

    @Test
    fun `dealer Tai win does not rotate dealer`() = runTest {
        viewModel.changeVariant(MahjongVariant.TAIWANESE)
        advanceUntilIdle()

        viewModel.selectWinner(Seat.EAST) // dealer (EAST) wins
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setTai(5)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()

        assertEquals(Seat.EAST, viewModel.gameState.first().dealer)
    }

    @Test
    fun `Riichi non-dealer win rotates dealer and resets honba`() = runTest {
        viewModel.changeVariant(MahjongVariant.JAPANESE_RIICHI)
        advanceUntilIdle()
        viewModel.setHonba(2)
        advanceUntilIdle()

        viewModel.selectWinner(Seat.SOUTH)
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setHan(3)
        viewModel.setFu(30)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()

        val state = viewModel.gameState.first()
        assertEquals(Seat.SOUTH, state.dealer)
        assertEquals(0, state.honbaCount)
        assertEquals(0, state.riichiSticksOnTable)
    }

    @Test
    fun `Riichi dealer win increments honba and keeps dealer`() = runTest {
        viewModel.changeVariant(MahjongVariant.JAPANESE_RIICHI)
        advanceUntilIdle()

        viewModel.selectWinner(Seat.EAST) // dealer wins
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setHan(3)
        viewModel.setFu(30)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()

        val state = viewModel.gameState.first()
        assertEquals(Seat.EAST, state.dealer)
        assertEquals(1, state.honbaCount)
    }

    // ─── Tai setters ─────────────────────────────────────────────────────────

    @Test
    fun `setTai clamps outside 1-30`() = runTest {
        viewModel.changeVariant(MahjongVariant.TAIWANESE)
        advanceUntilIdle()

        viewModel.setTai(0)
        assertEquals(1, (viewModel.formState.first() as FormState.Tai).taiCount)
        viewModel.setTai(100)
        assertEquals(30, (viewModel.formState.first() as FormState.Tai).taiCount)
    }

    // ─── Riichi setters ───────────────────────────────────────────────────────

    @Test
    fun `setHan clamps outside 1-13`() = runTest {
        viewModel.changeVariant(MahjongVariant.JAPANESE_RIICHI)
        advanceUntilIdle()

        viewModel.setHan(0)
        assertEquals(1, (viewModel.formState.first() as FormState.Riichi).han)
        viewModel.setHan(20)
        assertEquals(13, (viewModel.formState.first() as FormState.Riichi).han)
    }

    @Test
    fun `declareRiichi increments riichi sticks for Riichi variant`() = runTest {
        viewModel.changeVariant(MahjongVariant.JAPANESE_RIICHI)
        advanceUntilIdle()

        assertEquals(0, viewModel.gameState.first().riichiSticksOnTable)
        viewModel.declareRiichi(Seat.SOUTH)
        advanceUntilIdle()
        assertEquals(1, viewModel.gameState.first().riichiSticksOnTable)
    }

    @Test
    fun `declareRiichi is no-op for non-Riichi variant`() = runTest {
        // Default is HK New
        viewModel.declareRiichi(Seat.SOUTH)
        advanceUntilIdle()
        assertEquals(0, viewModel.gameState.first().riichiSticksOnTable)
    }
}

/** In-memory fake that bypasses DataStore. */
private class FakeGameRepository : GameRepository(dataStore = StubDataStore) {
    private val rules = MutableStateFlow<ScoreRules>(ScoreRules.HongKongNew())
    private val history = MutableStateFlow<List<CommittedRound>>(emptyList())
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val dealer = MutableStateFlow(Seat.EAST)
    private val honba = MutableStateFlow(0)
    private val riichiSticks = MutableStateFlow(0)

    override val rulesFlow: Flow<ScoreRules> = rules
    override val historyFlow: Flow<List<CommittedRound>> = history
    override val themeModeFlow: Flow<ThemeMode> = themeMode
    override val dealerFlow: Flow<Seat> = dealer
    override val honbaFlow: Flow<Int> = honba
    override val riichiSticksFlow: Flow<Int> = riichiSticks

    override suspend fun updateRules(rules: ScoreRules) {
        this.rules.value = rules
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    override suspend fun updateDealer(seat: Seat) {
        dealer.value = seat
    }

    override suspend fun updateHonba(count: Int) {
        honba.value = count.coerceAtLeast(0)
    }

    override suspend fun updateRiichiSticks(count: Int) {
        riichiSticks.value = count.coerceAtLeast(0)
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
        honba.value = 0
        riichiSticks.value = 0
    }
}

private val StubDataStore = object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
    override val data: Flow<androidx.datastore.preferences.core.Preferences>
        get() = kotlinx.coroutines.flow.emptyFlow()

    override suspend fun updateData(
        transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences,
    ): androidx.datastore.preferences.core.Preferences {
        throw UnsupportedOperationException("Fake repository should not touch the data store")
    }
}
