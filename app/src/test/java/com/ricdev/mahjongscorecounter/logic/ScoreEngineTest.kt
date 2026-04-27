package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreEngineTest {

    @Test
    fun `self draw applies three equal losses and winner gain sums to zero`() {
        val input = RoundInput(
            winner = Seat.EAST,
            winType = WinType.SELF_DRAW,
            amount = 4,
        )

        val result = ScoreEngine.calculate(input)

        assertEquals(12, result.deltas[Seat.EAST])
        assertEquals(-4, result.deltas[Seat.SOUTH])
        assertEquals(-4, result.deltas[Seat.WEST])
        assertEquals(-4, result.deltas[Seat.NORTH])
        assertEquals(0, result.deltas.values.sum())
    }

    @Test
    fun `discard win applies one payer loss and winner gain sums to zero`() {
        val input = RoundInput(
            winner = Seat.WEST,
            winType = WinType.DISCARD_WIN,
            payer = Seat.SOUTH,
            amount = 8,
        )

        val result = ScoreEngine.calculate(input)

        assertEquals(8, result.deltas[Seat.WEST])
        assertEquals(-8, result.deltas[Seat.SOUTH])
        assertEquals(0, result.deltas[Seat.EAST])
        assertEquals(0, result.deltas[Seat.NORTH])
        assertEquals(0, result.deltas.values.sum())
    }

    @Test
    fun `zero and negative amount are rejected`() {
        val zero = RoundInput(Seat.EAST, WinType.SELF_DRAW, amount = 0)
        val negative = RoundInput(Seat.EAST, WinType.SELF_DRAW, amount = -3)

        assertEquals(ValidationError.AmountBelowOne, ScoreEngine.validate(zero))
        assertEquals(ValidationError.AmountBelowOne, ScoreEngine.validate(negative))
    }

    @Test
    fun `discard payer missing or same as winner is rejected`() {
        val missing = RoundInput(
            winner = Seat.EAST,
            winType = WinType.DISCARD_WIN,
            amount = 5,
        )
        val sameAsWinner = RoundInput(
            winner = Seat.EAST,
            winType = WinType.DISCARD_WIN,
            payer = Seat.EAST,
            amount = 5,
        )

        assertEquals(ValidationError.PayerRequired, ScoreEngine.validate(missing))
        assertEquals(ValidationError.WinnerIsPayer, ScoreEngine.validate(sameAsWinner))
    }

    @Test
    fun `self draw rejects payer`() {
        val input = RoundInput(
            winner = Seat.EAST,
            winType = WinType.SELF_DRAW,
            payer = Seat.SOUTH,
            amount = 5,
        )

        assertEquals(ValidationError.PayerForbiddenForSelfDraw, ScoreEngine.validate(input))
    }

    @Test
    fun `calculate throws on invalid input`() {
        val input = RoundInput(
            winner = Seat.EAST,
            winType = WinType.DISCARD_WIN,
            amount = 1,
        )

        val ex = runCatching { ScoreEngine.calculate(input) }.exceptionOrNull()

        assertNotNull(ex)
        assertTrue(ex is IllegalArgumentException)
    }

    @Test
    fun `computeTotals empty returns zeros`() {
        val totals = ScoreEngine.computeTotals(emptyList())

        Seat.entries.forEach { seat ->
            assertEquals(0, totals[seat])
        }
    }

    @Test
    fun `validate accepts amount of 1`() {
        val input = RoundInput(Seat.EAST, WinType.SELF_DRAW, amount = 1)
        assertEquals(null, ScoreEngine.validate(input))
    }

    @Test
    fun `self draw result is always zero-sum`() {
        val input = RoundInput(Seat.NORTH, WinType.SELF_DRAW, amount = 100)
        val result = ScoreEngine.calculate(input)
        assertEquals(0, result.deltas.values.sum())
    }

    @Test
    fun `discard win result is always zero-sum`() {
        val input = RoundInput(Seat.WEST, WinType.DISCARD_WIN, payer = Seat.EAST, amount = 100)
        val result = ScoreEngine.calculate(input)
        assertEquals(0, result.deltas.values.sum())
    }

    @Test
    fun `totals accumulate across committed rounds`() {
        val selfDraw = RoundInput(
            winner = Seat.EAST,
            winType = WinType.SELF_DRAW,
            amount = 2,
        )
        val discard = RoundInput(
            winner = Seat.SOUTH,
            winType = WinType.DISCARD_WIN,
            payer = Seat.EAST,
            amount = 5,
        )
        val history = listOf(
            CommittedRound(selfDraw, ScoreEngine.calculate(selfDraw), timestampMillis = 1L),
            CommittedRound(discard, ScoreEngine.calculate(discard), timestampMillis = 2L),
        )

        val totals = ScoreEngine.computeTotals(history)

        assertEquals(1, totals[Seat.EAST])
        assertEquals(3, totals[Seat.SOUTH])
        assertEquals(-2, totals[Seat.WEST])
        assertEquals(-2, totals[Seat.NORTH])
        assertEquals(0, totals.values.sum())
    }
}
