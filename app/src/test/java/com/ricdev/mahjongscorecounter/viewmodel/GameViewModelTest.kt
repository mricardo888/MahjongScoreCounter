package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.data.GameRepository
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
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

    @Test
    fun `default form starts empty with zero totals`() = runTest {
        val form = viewModel.formState.first()
        val state = viewModel.gameState.first()

        assertNull(form.winner)
        assertEquals(WinType.SELF_DRAW, form.winType)
        assertNull(form.payer)
        assertEquals(0, form.amount)
        Seat.entries.forEach { seat ->
            assertEquals(0, state.totals[seat])
        }
        assertTrue(viewModel.preview.first() is PreviewState.Empty)
    }

    @Test
    fun `changing win type to self draw clears payer`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectPayer(Seat.SOUTH)

        assertEquals(Seat.SOUTH, viewModel.formState.first().payer)

        viewModel.selectWinType(WinType.SELF_DRAW)

        assertNull(viewModel.formState.first().payer)
    }

    @Test
    fun `changing winner clears matching payer`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectPayer(Seat.SOUTH)

        viewModel.selectWinner(Seat.SOUTH)

        assertNull(viewModel.formState.first().payer)
    }

    @Test
    fun `preview becomes valid when self draw has winner and amount`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.setAmount(4)
        advanceUntilIdle()

        val preview = viewModel.preview.first()

        assertTrue(preview is PreviewState.Valid)
    }

    @Test
    fun `preview is invalid when amount is missing`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        advanceUntilIdle()

        assertTrue(viewModel.preview.first() is PreviewState.Invalid)
    }

    @Test
    fun `preview is invalid when discard win missing payer`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.setAmount(5)
        advanceUntilIdle()

        assertTrue(viewModel.preview.first() is PreviewState.Invalid)
    }

    @Test
    fun `self draw commit updates totals and history`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        viewModel.setAmount(3)
        advanceUntilIdle()

        viewModel.commitRound()
        advanceUntilIdle()

        val state = viewModel.gameState.first()
        assertEquals(1, state.history.size)
        assertEquals(9, state.totals[Seat.EAST])
        assertEquals(-3, state.totals[Seat.SOUTH])
        assertEquals(-3, state.totals[Seat.WEST])
        assertEquals(-3, state.totals[Seat.NORTH])
    }

    @Test
    fun `discard commit updates payer and winner only`() = runTest {
        viewModel.selectWinner(Seat.WEST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectPayer(Seat.SOUTH)
        viewModel.setAmount(8)
        advanceUntilIdle()

        viewModel.commitRound()
        advanceUntilIdle()

        val state = viewModel.gameState.first()
        assertEquals(1, state.history.size)
        assertEquals(8, state.totals[Seat.WEST])
        assertEquals(-8, state.totals[Seat.SOUTH])
        assertEquals(0, state.totals[Seat.EAST])
        assertEquals(0, state.totals[Seat.NORTH])
    }

    @Test
    fun `commit resets amount and payer while preserving winner and win type`() = runTest {
        viewModel.selectWinner(Seat.WEST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectPayer(Seat.NORTH)
        viewModel.setAmount(7)
        advanceUntilIdle()

        viewModel.commitRound()
        advanceUntilIdle()

        val form = viewModel.formState.first()
        assertEquals(Seat.WEST, form.winner)
        assertEquals(WinType.DISCARD_WIN, form.winType)
        assertEquals(0, form.amount)
        assertNull(form.payer)
    }

    @Test
    fun `commitRound with invalid preview is a no-op`() = runTest {
        viewModel.selectWinner(Seat.EAST)
        advanceUntilIdle()

        viewModel.commitRound()
        advanceUntilIdle()

        assertEquals(0, viewModel.gameState.first().history.size)
    }

    @Test
    fun `totals accumulate across committed rounds`() = runTest {
        commitSelfDraw(winner = Seat.EAST, amount = 2)
        commitDiscard(winner = Seat.SOUTH, payer = Seat.EAST, amount = 5)

        val state = viewModel.gameState.first()

        assertEquals(2, state.history.size)
        assertEquals(1, state.totals[Seat.EAST])
        assertEquals(3, state.totals[Seat.SOUTH])
        assertEquals(-2, state.totals[Seat.WEST])
        assertEquals(-2, state.totals[Seat.NORTH])
    }

    @Test
    fun `undo last round restores expected totals and history`() = runTest {
        commitSelfDraw(winner = Seat.EAST, amount = 2)
        commitDiscard(winner = Seat.SOUTH, payer = Seat.EAST, amount = 5)

        viewModel.undoLast()
        advanceUntilIdle()

        val state = viewModel.gameState.first()
        assertEquals(1, state.history.size)
        assertEquals(6, state.totals[Seat.EAST])
        assertEquals(-2, state.totals[Seat.SOUTH])
        assertEquals(-2, state.totals[Seat.WEST])
        assertEquals(-2, state.totals[Seat.NORTH])
    }

    @Test
    fun `reset clears history totals and form`() = runTest {
        commitSelfDraw(winner = Seat.EAST, amount = 2)
        assertFalse(viewModel.gameState.first().history.isEmpty())

        viewModel.resetGame()
        advanceUntilIdle()

        val state = viewModel.gameState.first()
        val form = viewModel.formState.first()
        assertEquals(0, state.history.size)
        Seat.entries.forEach { seat ->
            assertEquals(0, state.totals[seat])
        }
        assertNull(form.winner)
        assertEquals(WinType.SELF_DRAW, form.winType)
        assertEquals(0, form.amount)
        assertNull(form.payer)
    }

    private fun TestScope.commitSelfDraw(winner: Seat, amount: Int) {
        viewModel.selectWinner(winner)
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setAmount(amount)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
    }

    private fun TestScope.commitDiscard(winner: Seat, payer: Seat, amount: Int) {
        viewModel.selectWinner(winner)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectPayer(payer)
        viewModel.setAmount(amount)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
    }
}

private class FakeGameRepository : GameRepository(dataStore = StubDataStore) {
    private val history = MutableStateFlow<List<CommittedRound>>(emptyList())
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    override val historyFlow: Flow<List<CommittedRound>> = history
    override val themeModeFlow: Flow<ThemeMode> = themeMode

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

private val StubDataStore = object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
    override val data: Flow<androidx.datastore.preferences.core.Preferences>
        get() = kotlinx.coroutines.flow.emptyFlow()

    override suspend fun updateData(
        transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences,
    ): androidx.datastore.preferences.core.Preferences {
        throw UnsupportedOperationException("Fake repository should not touch the data store")
    }
}
