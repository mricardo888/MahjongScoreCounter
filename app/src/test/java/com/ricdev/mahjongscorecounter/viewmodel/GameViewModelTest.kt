package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.data.GameRepository
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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

    private lateinit var dispatcher: StandardTestDispatcher
    private lateinit var repository: FakeGameRepository
    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeGameRepository()
        viewModel = GameViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `commit then undo round-trips totals and history`() = runTest(dispatcher) {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.SELF_DRAW)
        viewModel.setFan(3)
        advanceUntilIdle()

        viewModel.commitRound()
        advanceUntilIdle()

        val afterCommit = viewModel.gameState.first()
        assertEquals(1, afterCommit.history.size)
        assertEquals(48, afterCommit.totals[Seat.EAST])

        viewModel.undoLast()
        advanceUntilIdle()

        val afterUndo = viewModel.gameState.first()
        assertEquals(0, afterUndo.history.size)
        Seat.entries.forEach { seat ->
            assertEquals(0, afterUndo.totals[seat])
        }
    }

    @Test
    fun `selectWinType SELF_DRAW clears previously set discarder`() = runTest(dispatcher) {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        viewModel.selectDiscarder(Seat.SOUTH)
        assertEquals(Seat.SOUTH, viewModel.formState.first().discarder)

        viewModel.selectWinType(WinType.SELF_DRAW)
        assertNull(viewModel.formState.first().discarder)
    }

    @Test
    fun `commitRound with valid form resets fan and clears discarder, keeping winner`() =
        runTest(dispatcher) {
            viewModel.selectWinner(Seat.WEST)
            viewModel.selectWinType(WinType.DISCARD_WIN)
            viewModel.selectDiscarder(Seat.NORTH)
            viewModel.setFan(5)
            advanceUntilIdle()

            viewModel.commitRound()
            advanceUntilIdle()

            val form = viewModel.formState.first()
            assertEquals(Seat.WEST, form.winner)
            assertEquals(1, form.fanCount)
            assertNull(form.discarder)

            val state = viewModel.gameState.first()
            assertEquals(1, state.history.size)
        }

    @Test
    fun `commitRound with invalid preview is a no-op`() = runTest(dispatcher) {
        // no winner selected → preview is Empty
        viewModel.setFan(3)
        advanceUntilIdle()
        viewModel.commitRound()
        advanceUntilIdle()
        val state = viewModel.gameState.first()
        assertEquals(0, state.history.size)
    }

    @Test
    fun `setFan clamps outside 1-20`() = runTest(dispatcher) {
        viewModel.setFan(0)
        assertEquals(1, viewModel.formState.first().fanCount)
        viewModel.setFan(50)
        assertEquals(20, viewModel.formState.first().fanCount)
    }

    @Test
    fun `preview becomes Valid when winner selected for self draw`() = runTest(dispatcher) {
        assertTrue(viewModel.preview.first() is PreviewState.Empty)
        viewModel.selectWinner(Seat.EAST)
        advanceUntilIdle()
        assertTrue(viewModel.preview.first() is PreviewState.Valid)
    }

    @Test
    fun `preview is Invalid when discard win missing discarder`() = runTest(dispatcher) {
        viewModel.selectWinner(Seat.EAST)
        viewModel.selectWinType(WinType.DISCARD_WIN)
        advanceUntilIdle()
        val preview = viewModel.preview.first()
        assertTrue(preview is PreviewState.Invalid)
    }

    @Test
    fun `resetGame clears history and form`() = runTest(dispatcher) {
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
}

/** In-memory fake that bypasses DataStore. */
private class FakeGameRepository : GameRepository(dataStore = StubDataStore) {
    private val rules = MutableStateFlow(ScoreRules())
    private val history = MutableStateFlow<List<CommittedRound>>(emptyList())

    override val rulesFlow: Flow<ScoreRules> = rules
    override val historyFlow: Flow<List<CommittedRound>> = history

    override suspend fun updateRules(rules: ScoreRules) {
        this.rules.value = rules
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
