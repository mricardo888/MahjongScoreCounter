package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreEngineTest {

    private val defaultRules = ScoreRules()

    @Test
    fun `default rules use half-value self draw base`() {
        assertEquals(4, defaultRules.selfDrawBase)
        assertEquals(8, defaultRules.discardWinBase)
    }

    // ---------- amountForFan ----------

    @Test
    fun `amountForFan doubles each fan`() {
        assertEquals(8, ScoreEngine.amountForFan(8, 1))
        assertEquals(16, ScoreEngine.amountForFan(8, 2))
        assertEquals(32, ScoreEngine.amountForFan(8, 3))
        assertEquals(512, ScoreEngine.amountForFan(8, 7))
    }

    // ---------- calculate: Self Draw ----------

    @Test
    fun `self draw 7-fan east winner pays 256 each`() {
        val input = RoundInput(
            winner = Seat.EAST,
            winType = WinType.SELF_DRAW,
            fanCount = 7,
        )
        val result = ScoreEngine.calculate(input, defaultRules)
        assertEquals(768, result.deltas[Seat.EAST])
        assertEquals(-256, result.deltas[Seat.SOUTH])
        assertEquals(-256, result.deltas[Seat.WEST])
        assertEquals(-256, result.deltas[Seat.NORTH])
        assertEquals(256, result.perLoserAmount)
        assertEquals(768, result.totalAmount)
    }

    // ---------- calculate: Discard Win ----------

    @Test
    fun `discard win 3-fan east winner south discarder`() {
        val input = RoundInput(
            winner = Seat.EAST,
            winType = WinType.DISCARD_WIN,
            fanCount = 3,
            discarder = Seat.SOUTH,
        )
        val result = ScoreEngine.calculate(input, defaultRules)
        assertEquals(32, result.deltas[Seat.EAST])
        assertEquals(-32, result.deltas[Seat.SOUTH])
        assertEquals(0, result.deltas[Seat.WEST])
        assertEquals(0, result.deltas[Seat.NORTH])
        assertEquals(32, result.perLoserAmount)
        assertEquals(32, result.totalAmount)
    }

    // ---------- property test: delta sum is always zero ----------

    @Test
    fun `delta sum is zero across all fan x winType x winner x discarder combos`() {
        for (fan in 1..10) {
            for (winner in Seat.entries) {
                // self draw: no discarder
                val selfDraw = RoundInput(
                    winner = winner,
                    winType = WinType.SELF_DRAW,
                    fanCount = fan,
                )
                val selfDrawSum = ScoreEngine.calculate(selfDraw, defaultRules).deltas.values.sum()
                assertEquals("self draw fan=$fan winner=$winner", 0, selfDrawSum)

                // discard win: every non-winner seat as discarder
                for (discarder in Seat.entries) {
                    if (discarder == winner) continue
                    val discardWin = RoundInput(
                        winner = winner,
                        winType = WinType.DISCARD_WIN,
                        fanCount = fan,
                        discarder = discarder,
                    )
                    val discardSum = ScoreEngine.calculate(discardWin, defaultRules).deltas.values.sum()
                    assertEquals(
                        "discard win fan=$fan winner=$winner discarder=$discarder",
                        0,
                        discardSum,
                    )
                }
            }
        }
    }

    // ---------- computeTotals ----------

    @Test
    fun `computeTotals on empty history returns all seats at zero`() {
        val totals = ScoreEngine.computeTotals(emptyList())
        assertEquals(4, totals.size)
        Seat.entries.forEach { seat ->
            assertEquals("seat $seat", 0, totals[seat])
        }
    }

    @Test
    fun `commit then undo round-trips totals via computeTotals`() {
        val input = RoundInput(
            winner = Seat.WEST,
            winType = WinType.DISCARD_WIN,
            fanCount = 5,
            discarder = Seat.NORTH,
        )
        val result = ScoreEngine.calculate(input, defaultRules)
        val committed = CommittedRound(input, result, timestampMillis = 1_000L)

        val afterCommit = ScoreEngine.computeTotals(listOf(committed))
        assertEquals(128, afterCommit[Seat.WEST])
        assertEquals(-128, afterCommit[Seat.NORTH])

        val afterUndo = ScoreEngine.computeTotals(emptyList())
        Seat.entries.forEach { seat ->
            assertEquals(0, afterUndo[seat])
        }
    }

    @Test
    fun `computeTotals sums multiple rounds`() {
        val first = CommittedRound(
            input = RoundInput(Seat.EAST, WinType.SELF_DRAW, 2),
            result = ScoreEngine.calculate(
                RoundInput(Seat.EAST, WinType.SELF_DRAW, 2),
                defaultRules,
            ),
            timestampMillis = 1L,
        )
        val second = CommittedRound(
            input = RoundInput(Seat.SOUTH, WinType.DISCARD_WIN, 3, Seat.EAST),
            result = ScoreEngine.calculate(
                RoundInput(Seat.SOUTH, WinType.DISCARD_WIN, 3, Seat.EAST),
                defaultRules,
            ),
            timestampMillis = 2L,
        )
        val totals = ScoreEngine.computeTotals(listOf(first, second))
        // First round: EAST +24, S/W/N −8 each
        // Second round: SOUTH +32, EAST −32
        assertEquals(24 - 32, totals[Seat.EAST])
        assertEquals(-8 + 32, totals[Seat.SOUTH])
        assertEquals(-8, totals[Seat.WEST])
        assertEquals(-8, totals[Seat.NORTH])
        assertEquals(0, totals.values.sum())
    }

    // ---------- validate: every branch ----------

    @Test
    fun `validate passes on valid self draw`() {
        val input = RoundInput(Seat.EAST, WinType.SELF_DRAW, 1)
        assertNull(ScoreEngine.validate(input, defaultRules))
    }

    @Test
    fun `validate passes on valid discard win`() {
        val input = RoundInput(Seat.EAST, WinType.DISCARD_WIN, 1, Seat.SOUTH)
        assertNull(ScoreEngine.validate(input, defaultRules))
    }

    @Test
    fun `validate rejects fan below one`() {
        val input = RoundInput(Seat.EAST, WinType.SELF_DRAW, 0)
        assertEquals(ValidationError.FanBelowOne, ScoreEngine.validate(input, defaultRules))
    }

    @Test
    fun `validate rejects discarder required`() {
        val input = RoundInput(Seat.EAST, WinType.DISCARD_WIN, 1, discarder = null)
        assertEquals(ValidationError.DiscarderRequired, ScoreEngine.validate(input, defaultRules))
    }

    @Test
    fun `validate rejects discarder forbidden for self draw`() {
        val input = RoundInput(Seat.EAST, WinType.SELF_DRAW, 1, discarder = Seat.SOUTH)
        assertEquals(
            ValidationError.DiscarderForbiddenForSelfDraw,
            ScoreEngine.validate(input, defaultRules),
        )
    }

    @Test
    fun `validate rejects winner equals discarder`() {
        val input = RoundInput(Seat.EAST, WinType.DISCARD_WIN, 1, discarder = Seat.EAST)
        assertEquals(ValidationError.WinnerIsDiscarder, ScoreEngine.validate(input, defaultRules))
    }

    @Test
    fun `validate rejects negative base rules`() {
        val rules = ScoreRules(selfDrawBase = -1, discardWinBase = 8)
        val input = RoundInput(Seat.EAST, WinType.SELF_DRAW, 1)
        assertEquals(ValidationError.NegativeBase, ScoreEngine.validate(input, rules))
    }

    @Test
    fun `calculate throws on invalid input`() {
        val input = RoundInput(Seat.EAST, WinType.DISCARD_WIN, 1, discarder = null)
        val ex = runCatching { ScoreEngine.calculate(input, defaultRules) }.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is IllegalArgumentException)
    }
}
