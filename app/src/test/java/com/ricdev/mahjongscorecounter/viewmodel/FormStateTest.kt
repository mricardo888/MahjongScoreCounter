package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormStateTest {

    @Test
    fun `withWinner clears payer when payer matches new winner`() {
        val state = FormState(winner = Seat.EAST, payer = Seat.SOUTH)
            .withWinner(Seat.SOUTH)

        assertEquals(Seat.SOUTH, state.winner)
        assertNull(state.payer)
    }

    @Test
    fun `withWinner keeps payer when payer does not match new winner`() {
        val state = FormState(winner = Seat.EAST, payer = Seat.WEST)
            .withWinner(Seat.SOUTH)

        assertEquals(Seat.SOUTH, state.winner)
        assertEquals(Seat.WEST, state.payer)
    }

    @Test
    fun `withWinType SELF_DRAW clears payer`() {
        val state = FormState(winType = WinType.DISCARD_WIN, payer = Seat.NORTH)
            .withWinType(WinType.SELF_DRAW)

        assertEquals(WinType.SELF_DRAW, state.winType)
        assertNull(state.payer)
    }

    @Test
    fun `withWinType DISCARD_WIN preserves existing payer`() {
        val state = FormState(winType = WinType.SELF_DRAW, payer = null)
            .withWinType(WinType.DISCARD_WIN)

        assertEquals(WinType.DISCARD_WIN, state.winType)
        assertNull(state.payer)
    }

    @Test
    fun `withPayer is no-op when seat equals winner`() {
        val original = FormState(winner = Seat.EAST, payer = null)
        val result = original.withPayer(Seat.EAST)

        assertEquals(original, result)
    }

    @Test
    fun `withPayer accepts null to clear payer`() {
        val state = FormState(winner = Seat.EAST, payer = Seat.SOUTH)
            .withPayer(null)

        assertNull(state.payer)
    }

    @Test
    fun `withPayer sets a valid payer`() {
        val state = FormState(winner = Seat.EAST)
            .withPayer(Seat.WEST)

        assertEquals(Seat.WEST, state.payer)
    }
}
