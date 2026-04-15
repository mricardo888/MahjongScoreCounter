package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreRulesSerializationTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `all rules variants round-trip through JSON`() {
        val samples: List<ScoreRules> = listOf(
            ScoreRules.HongKongNew(discardBase = 8, selfDrawBase = 4, maxFan = 10),
            ScoreRules.Taiwanese(base = 10, perTai = 10, minTai = 5, selfDrawBonusTai = 1, dealerMultiplier = 2),
            ScoreRules.JapaneseRiichi(kiriageMangan = true, honbaValue = 300, riichiStickValue = 1000),
            ScoreRules.Hokkien(base = 2, perUnit = 3, maxUnits = 16, dealerDoubles = true),
            ScoreRules.Shanghai(base = 2, maxFan = 13),
            ScoreRules.Sichuan(base = 1, maxFan = 5),
            ScoreRules.Singaporean(discardBase = 2, selfDrawBase = 1, flowerBonus = 1, animalBonus = 2),
        )
        samples.forEach { rules ->
            val encoded = json.encodeToString(ScoreRules.serializer(), rules)
            val decoded = json.decodeFromString(ScoreRules.serializer(), encoded)
            assertEquals(rules, decoded)
        }
    }

    @Test
    fun `round input variants round-trip`() {
        val samples: List<RoundInput> = listOf(
            RoundInput.Fan(Seat.EAST, WinType.SELF_DRAW, fanCount = 3),
            RoundInput.Fan(Seat.WEST, WinType.DISCARD_WIN, discarder = Seat.EAST, fanCount = 5, flowerCount = 1, animalCount = 2),
            RoundInput.Tai(Seat.SOUTH, WinType.SELF_DRAW, taiCount = 6, dealer = Seat.EAST),
            RoundInput.Riichi(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST, han = 4, fu = 30, dealer = Seat.EAST, honbaCount = 1, riichiSticks = 2),
        )
        samples.forEach { input ->
            val encoded = json.encodeToString(RoundInput.serializer(), input)
            val decoded = json.decodeFromString(RoundInput.serializer(), encoded)
            assertEquals(input, decoded)
        }
    }

    @Test
    fun `committed round round-trips preserving polymorphic input`() {
        val input = RoundInput.Riichi(
            Seat.SOUTH, WinType.SELF_DRAW,
            han = 3, fu = 30, dealer = Seat.EAST, honbaCount = 1, riichiSticks = 1,
        )
        val result = ScoreEngine.calculate(input, ScoreRules.JapaneseRiichi())
        val committed = CommittedRound(input, result, timestampMillis = 123L)
        val encoded = json.encodeToString(CommittedRound.serializer(), committed)
        val decoded = json.decodeFromString(CommittedRound.serializer(), encoded)
        assertEquals(committed, decoded)
    }
}
