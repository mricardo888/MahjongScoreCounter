package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object RiichiEngine {

    fun validate(input: RoundInput, rules: ScoreRules.JapaneseRiichi): ValidationError? {
        if (rules.honbaValue < 0 || rules.riichiStickValue < 0) return ValidationError.NegativeBase
        val r = input as? RoundInput.Riichi ?: return ValidationError.InputTypeMismatch
        if (r.han < 1) return ValidationError.HanBelowOne
        if (r.fu < 20) return ValidationError.FuBelowMin
        if (!isValidFu(r.fu)) return ValidationError.FuInvalid
        if (r.honbaCount < 0 || r.riichiSticks < 0) return ValidationError.NegativeCount
        return validateDiscarder(input)
    }

    fun calculate(input: RoundInput.Riichi, rules: ScoreRules.JapaneseRiichi): RoundResult {
        val basic = basicPoints(input.han, input.fu, rules.kiriageMangan)
        val winnerIsDealer = input.winner == input.dealer

        val deltas = zeroDeltas()
        val honbaPerPayer = rules.honbaValue / 3
        when (input.winType) {
            WinType.SELF_DRAW -> {
                if (winnerIsDealer) {
                    val perLoser = roundUp100(basic * 2) + honbaPerPayer * input.honbaCount
                    Seat.entries.forEach { seat ->
                        if (seat == input.winner) return@forEach
                        deltas[seat] = -perLoser
                    }
                } else {
                    val dealerPays = roundUp100(basic * 2) + honbaPerPayer * input.honbaCount
                    val nonDealerPays = roundUp100(basic * 1) + honbaPerPayer * input.honbaCount
                    Seat.entries.forEach { seat ->
                        if (seat == input.winner) return@forEach
                        deltas[seat] = if (seat == input.dealer) -dealerPays else -nonDealerPays
                    }
                }
                deltas[input.winner] = -deltas.values.filter { it < 0 }.sum()
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                val base = if (winnerIsDealer) basic * 6 else basic * 4
                val payment = roundUp100(base) + rules.honbaValue * input.honbaCount
                deltas[input.winner] = payment
                deltas[discarder] = -payment
            }
        }

        val stickBonus = input.riichiSticks * rules.riichiStickValue
        return RoundResult(
            deltas = deltas,
            winner = input.winner,
            winType = input.winType,
            stickBonus = stickBonus,
        ).also {
            val sum = it.deltas.values.sum()
            check(sum == 0) { "Riichi deltas must sum to zero, got $sum" }
        }
    }

    internal fun basicPoints(han: Int, fu: Int, kiriageMangan: Boolean): Int {
        val limit = when {
            han >= 13 -> 8000
            han >= 11 -> 6000
            han >= 8 -> 4000
            han >= 6 -> 3000
            han == 5 -> 2000
            else -> -1
        }
        if (limit > 0) return limit

        val raw = fu * (1 shl (han + 2))
        if (kiriageMangan && ((han == 4 && fu >= 30) || (han == 3 && fu >= 70))) return 2000
        return minOf(raw, 2000)
    }

    private fun roundUp100(value: Int): Int = ((value + 99) / 100) * 100

    private fun isValidFu(fu: Int): Boolean = fu == 20 || fu == 25 || fu % 10 == 0
}
