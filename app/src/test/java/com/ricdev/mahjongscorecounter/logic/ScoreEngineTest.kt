package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.logic.variants.RiichiEngine
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

    // ---------- Hong Kong New Style ----------

    private val hkDefaults = ScoreRules.HongKongNew()

    @Test
    fun `hk new defaults to discardBase=2 selfDrawBase=1 no cap`() {
        assertEquals(2, hkDefaults.discardBase)
        assertEquals(1, hkDefaults.selfDrawBase)
        assertNull(hkDefaults.maxFan)
    }

    @Test
    fun `hk new self draw 3-fan linear`() {
        val input = RoundInput.Fan(Seat.EAST, WinType.SELF_DRAW, fanCount = 3)
        val result = ScoreEngine.calculate(input, hkDefaults)
        // perLoser = 1 * 3 = 3, winner = 9
        assertEquals(9, result.deltas[Seat.EAST])
        assertEquals(-3, result.deltas[Seat.SOUTH])
        assertEquals(-3, result.deltas[Seat.WEST])
        assertEquals(-3, result.deltas[Seat.NORTH])
    }

    @Test
    fun `hk new discard win 5-fan`() {
        val input = RoundInput.Fan(Seat.WEST, WinType.DISCARD_WIN, discarder = Seat.SOUTH, fanCount = 5)
        val result = ScoreEngine.calculate(input, hkDefaults)
        // 2 * 5 = 10
        assertEquals(10, result.deltas[Seat.WEST])
        assertEquals(-10, result.deltas[Seat.SOUTH])
        assertEquals(0, result.deltas[Seat.EAST])
        assertEquals(0, result.deltas[Seat.NORTH])
    }

    @Test
    fun `hk new max fan caps payment`() {
        val rules = ScoreRules.HongKongNew(discardBase = 2, selfDrawBase = 1, maxFan = 10)
        val input = RoundInput.Fan(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST, fanCount = 15)
        val result = ScoreEngine.calculate(input, rules)
        assertEquals(20, result.deltas[Seat.EAST])
    }

    @Test
    fun `hk new delta sum zero across combinations`() {
        for (fan in 1..13) {
            for (winner in Seat.entries) {
                val sd = RoundInput.Fan(winner, WinType.SELF_DRAW, fanCount = fan)
                assertEquals(0, ScoreEngine.calculate(sd, hkDefaults).deltas.values.sum())
                for (discarder in Seat.entries) {
                    if (discarder == winner) continue
                    val dw = RoundInput.Fan(winner, WinType.DISCARD_WIN, discarder = discarder, fanCount = fan)
                    assertEquals(0, ScoreEngine.calculate(dw, hkDefaults).deltas.values.sum())
                }
            }
        }
    }

    @Test
    fun `hk new rejects mismatched input type`() {
        val input = RoundInput.Tai(Seat.EAST, WinType.SELF_DRAW, taiCount = 5)
        assertEquals(ValidationError.InputTypeMismatch, ScoreEngine.validate(input, hkDefaults))
    }

    // ---------- Taiwanese 16-tile ----------

    private val twDefaults = ScoreRules.Taiwanese()

    @Test
    fun `taiwanese dealer self-draw at 6 tai each non-dealer pays double`() {
        val input = RoundInput.Tai(Seat.EAST, WinType.SELF_DRAW, taiCount = 6, dealer = Seat.EAST)
        val result = ScoreEngine.calculate(input, twDefaults)
        // effTai = 6 + 1 (self-draw bonus) = 7; unit = 1 + 7*1 = 8
        // winnerIsDealer: each non-dealer pays 8 * 2 = 16
        assertEquals(-16, result.deltas[Seat.SOUTH])
        assertEquals(-16, result.deltas[Seat.WEST])
        assertEquals(-16, result.deltas[Seat.NORTH])
        assertEquals(48, result.deltas[Seat.EAST])
    }

    @Test
    fun `taiwanese non-dealer wins by discard from dealer pays double`() {
        val input = RoundInput.Tai(
            winner = Seat.SOUTH, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            taiCount = 5, dealer = Seat.EAST,
        )
        val result = ScoreEngine.calculate(input, twDefaults)
        // unit = 1 + 5*1 = 6 (no self-draw bonus); dealer involved → × 2 = 12
        assertEquals(12, result.deltas[Seat.SOUTH])
        assertEquals(-12, result.deltas[Seat.EAST])
    }

    @Test
    fun `taiwanese non-dealer self-draw with dealer as one loser`() {
        val input = RoundInput.Tai(Seat.SOUTH, WinType.SELF_DRAW, taiCount = 5, dealer = Seat.EAST)
        val result = ScoreEngine.calculate(input, twDefaults)
        // effTai = 6, unit = 7
        // EAST (dealer) pays 14, others pay 7
        assertEquals(-14, result.deltas[Seat.EAST])
        assertEquals(-7, result.deltas[Seat.WEST])
        assertEquals(-7, result.deltas[Seat.NORTH])
        assertEquals(28, result.deltas[Seat.SOUTH])
    }

    @Test
    fun `taiwanese rejects tai below min`() {
        val input = RoundInput.Tai(Seat.EAST, WinType.SELF_DRAW, taiCount = 3, dealer = Seat.EAST)
        assertEquals(ValidationError.TaiBelowMin, ScoreEngine.validate(input, twDefaults))
    }

    // ---------- Hokkien ----------

    private val hkDefaults2 = ScoreRules.Hokkien()

    @Test
    fun `hokkien default self-draw linear no dealer bonus`() {
        val input = RoundInput.Tai(Seat.WEST, WinType.SELF_DRAW, taiCount = 3, dealer = Seat.EAST)
        val result = ScoreEngine.calculate(input, hkDefaults2)
        // unit = 1 + 3*1 = 4; no dealer doubling by default
        assertEquals(-4, result.deltas[Seat.EAST])
        assertEquals(-4, result.deltas[Seat.SOUTH])
        assertEquals(12, result.deltas[Seat.WEST])
        assertEquals(-4, result.deltas[Seat.NORTH])
    }

    @Test
    fun `hokkien respects max units cap`() {
        val rules = ScoreRules.Hokkien(base = 1, perUnit = 2, maxUnits = 5)
        val input = RoundInput.Tai(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.SOUTH, taiCount = 20)
        val result = ScoreEngine.calculate(input, rules)
        // capped at 5: unit = 1 + 5*2 = 11
        assertEquals(11, result.deltas[Seat.EAST])
        assertEquals(-11, result.deltas[Seat.SOUTH])
    }

    @Test
    fun `hokkien with dealer doubling applies 2x for dealer involvement`() {
        val rules = ScoreRules.Hokkien(dealerDoubles = true)
        val input = RoundInput.Tai(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.SOUTH, taiCount = 2, dealer = Seat.EAST)
        val result = ScoreEngine.calculate(input, rules)
        // unit = 1 + 2*1 = 3; dealer won → × 2 = 6
        assertEquals(6, result.deltas[Seat.EAST])
        assertEquals(-6, result.deltas[Seat.SOUTH])
    }

    // ---------- Shanghai ----------

    private val shDefaults = ScoreRules.Shanghai()

    @Test
    fun `shanghai doubling formula caps at maxFan`() {
        val rules = ScoreRules.Shanghai(base = 1, maxFan = 8)
        val input = RoundInput.Fan(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST, fanCount = 10)
        val result = ScoreEngine.calculate(input, rules)
        // capped at 8: 1 * 2^7 = 128
        assertEquals(128, result.deltas[Seat.EAST])
        assertEquals(-128, result.deltas[Seat.WEST])
    }

    // ---------- Sichuan ----------

    @Test
    fun `sichuan defaults doubling with 5 fan cap`() {
        val rules = ScoreRules.Sichuan()
        val input = RoundInput.Fan(Seat.EAST, WinType.SELF_DRAW, fanCount = 10)
        val result = ScoreEngine.calculate(input, rules)
        // cap=5: 1 * 2^4 = 16 per loser, winner = 48
        assertEquals(48, result.deltas[Seat.EAST])
        assertEquals(-16, result.deltas[Seat.SOUTH])
    }

    // ---------- Singaporean ----------

    @Test
    fun `singaporean adds flower and animal bonuses`() {
        val rules = ScoreRules.Singaporean()
        val input = RoundInput.Fan(
            Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST,
            fanCount = 3, flowerCount = 2, animalCount = 1,
        )
        val result = ScoreEngine.calculate(input, rules)
        // 2 * 3 + 2*1 + 1*2 = 10
        assertEquals(10, result.deltas[Seat.EAST])
        assertEquals(-10, result.deltas[Seat.WEST])
    }

    // ---------- Japanese Riichi ----------

    private val rDefaults = ScoreRules.JapaneseRiichi()

    @Test
    fun `riichi non-dealer tsumo 3 han 30 fu`() {
        // basic = 30 * 2^5 = 960; non-dealer tsumo: dealer 2*960=1920 → 2000, each non-dealer 960 → 1000
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.SELF_DRAW,
            han = 3, fu = 30, dealer = Seat.EAST,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        assertEquals(-2000, result.deltas[Seat.EAST])
        assertEquals(-1000, result.deltas[Seat.WEST])
        assertEquals(-1000, result.deltas[Seat.NORTH])
        assertEquals(4000, result.deltas[Seat.SOUTH])
    }

    @Test
    fun `riichi mangan non-dealer ron 5 han any fu`() {
        // 5 han → mangan basic 2000; non-dealer ron: 2000 * 4 = 8000
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            han = 5, fu = 30, dealer = Seat.EAST,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        assertEquals(8000, result.deltas[Seat.SOUTH])
        assertEquals(-8000, result.deltas[Seat.EAST])
    }

    @Test
    fun `riichi dealer ron 4 han 30 fu is 11600`() {
        // basic = 30 * 2^6 = 1920; dealer ron: 1920 * 6 = 11520 → 11600
        val input = RoundInput.Riichi(
            winner = Seat.EAST, winType = WinType.DISCARD_WIN, discarder = Seat.WEST,
            han = 4, fu = 30, dealer = Seat.EAST,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        assertEquals(11600, result.deltas[Seat.EAST])
        assertEquals(-11600, result.deltas[Seat.WEST])
    }

    @Test
    fun `riichi yakuman non-dealer ron is 32000`() {
        val input = RoundInput.Riichi(
            winner = Seat.WEST, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            han = 13, fu = 30, dealer = Seat.EAST,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        // basic = 8000; non-dealer ron = 8000*4 = 32000
        assertEquals(32000, result.deltas[Seat.WEST])
        assertEquals(-32000, result.deltas[Seat.EAST])
    }

    @Test
    fun `riichi honba adds 300 per counter on ron`() {
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            han = 5, fu = 30, dealer = Seat.EAST, honbaCount = 2,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        // mangan 8000 + 2*300 = 8600
        assertEquals(8600, result.deltas[Seat.SOUTH])
        assertEquals(-8600, result.deltas[Seat.EAST])
    }

    @Test
    fun `riichi honba adds 100 per counter per payer on tsumo`() {
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.SELF_DRAW,
            han = 5, fu = 30, dealer = Seat.EAST, honbaCount = 1,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        // mangan non-dealer tsumo: dealer 4000, non-dealer 2000; +100 per payer
        assertEquals(-4100, result.deltas[Seat.EAST])
        assertEquals(-2100, result.deltas[Seat.WEST])
        assertEquals(-2100, result.deltas[Seat.NORTH])
        assertEquals(8300, result.deltas[Seat.SOUTH])
    }

    @Test
    fun `riichi riichi sticks surfaced as stickBonus`() {
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            han = 3, fu = 30, dealer = Seat.EAST, riichiSticks = 2,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        assertEquals(2000, result.stickBonus)
        assertEquals(0, result.deltas.values.sum())
    }

    @Test
    fun `riichi kiriage mangan promotes 4 han 30 fu`() {
        val rules = ScoreRules.JapaneseRiichi(kiriageMangan = true)
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            han = 4, fu = 30, dealer = Seat.EAST,
        )
        val result = ScoreEngine.calculate(input, rules)
        // promoted: basic 2000, non-dealer ron = 8000
        assertEquals(8000, result.deltas[Seat.SOUTH])
    }

    @Test
    fun `riichi basicPoints table`() {
        assertEquals(2000, RiichiEngine.basicPoints(5, 30, false))
        assertEquals(3000, RiichiEngine.basicPoints(6, 30, false))
        assertEquals(4000, RiichiEngine.basicPoints(8, 30, false))
        assertEquals(6000, RiichiEngine.basicPoints(11, 30, false))
        assertEquals(8000, RiichiEngine.basicPoints(13, 30, false))
        // han < 5 computes fu*2^(han+2) capped at 2000
        assertEquals(480, RiichiEngine.basicPoints(2, 30, false))  // 30 * 16 = 480
        assertEquals(1920, RiichiEngine.basicPoints(4, 30, false)) // 30 * 64 = 1920
        assertEquals(2000, RiichiEngine.basicPoints(4, 40, false)) // 40 * 64 = 2560 → cap 2000
    }

    @Test
    fun `riichi rejects fu below 20`() {
        val input = RoundInput.Riichi(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST, han = 1, fu = 10)
        assertEquals(ValidationError.FuBelowMin, ScoreEngine.validate(input, rDefaults))
    }

    @Test
    fun `riichi rejects invalid fu`() {
        val input = RoundInput.Riichi(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST, han = 1, fu = 33)
        assertEquals(ValidationError.FuInvalid, ScoreEngine.validate(input, rDefaults))
    }

    @Test
    fun `riichi rejects han below 1`() {
        val input = RoundInput.Riichi(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.WEST, han = 0, fu = 30)
        assertEquals(ValidationError.HanBelowOne, ScoreEngine.validate(input, rDefaults))
    }

    // ---------- shared validation paths ----------

    @Test
    fun `shared discarder required for discard-win`() {
        val input = RoundInput.Fan(Seat.EAST, WinType.DISCARD_WIN, fanCount = 1)
        assertEquals(ValidationError.DiscarderRequired, ScoreEngine.validate(input, hkDefaults))
    }

    @Test
    fun `shared discarder forbidden for self-draw`() {
        val input = RoundInput.Fan(Seat.EAST, WinType.SELF_DRAW, discarder = Seat.SOUTH, fanCount = 1)
        assertEquals(ValidationError.DiscarderForbiddenForSelfDraw, ScoreEngine.validate(input, hkDefaults))
    }

    @Test
    fun `shared winner equals discarder rejected`() {
        val input = RoundInput.Fan(Seat.EAST, WinType.DISCARD_WIN, discarder = Seat.EAST, fanCount = 1)
        assertEquals(ValidationError.WinnerIsDiscarder, ScoreEngine.validate(input, hkDefaults))
    }

    @Test
    fun `calculate throws on invalid input`() {
        val input = RoundInput.Fan(Seat.EAST, WinType.DISCARD_WIN, fanCount = 1)
        val ex = runCatching { ScoreEngine.calculate(input, hkDefaults) }.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is IllegalArgumentException)
    }

    // ---------- computeTotals ----------

    @Test
    fun `computeTotals empty returns zeros`() {
        val totals = ScoreEngine.computeTotals(emptyList())
        Seat.entries.forEach { assertEquals(0, totals[it]) }
    }

    @Test
    fun `computeTotals sums deltas and stick bonuses`() {
        val input = RoundInput.Riichi(
            winner = Seat.SOUTH, winType = WinType.DISCARD_WIN, discarder = Seat.EAST,
            han = 3, fu = 30, dealer = Seat.EAST, riichiSticks = 1,
        )
        val result = ScoreEngine.calculate(input, rDefaults)
        val committed = CommittedRound(input, result, timestampMillis = 1L)
        val totals = ScoreEngine.computeTotals(listOf(committed))
        // Ron 3han 30fu non-dealer: basic 960 × 4 = 3840 → 3900. +stick 1000 for winner.
        assertEquals(3900 + 1000, totals[Seat.SOUTH])
        assertEquals(-3900, totals[Seat.EAST])
    }
}
