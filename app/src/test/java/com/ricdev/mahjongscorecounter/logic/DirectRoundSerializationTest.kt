package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectRoundSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `round input round-trips through direct JSON shape`() {
        val samples = listOf(
            RoundInput(Seat.EAST, WinType.SELF_DRAW, amount = 3),
            RoundInput(Seat.WEST, WinType.DISCARD_WIN, payer = Seat.SOUTH, amount = 8),
        )

        samples.forEach { input ->
            val encoded = json.encodeToString(RoundInput.serializer(), input)
            val decoded = json.decodeFromString(RoundInput.serializer(), encoded)
            assertEquals(input, decoded)
        }
    }

    @Test
    fun `committed round round-trips preserving direct input and result`() {
        val input = RoundInput(
            winner = Seat.SOUTH,
            winType = WinType.DISCARD_WIN,
            payer = Seat.NORTH,
            amount = 6,
        )
        val committed = CommittedRound(
            input = input,
            result = ScoreEngine.calculate(input),
            timestampMillis = 123L,
        )

        val encoded = json.encodeToString(CommittedRound.serializer(), committed)
        val decoded = json.decodeFromString(CommittedRound.serializer(), encoded)

        assertEquals(committed, decoded)
    }
}
