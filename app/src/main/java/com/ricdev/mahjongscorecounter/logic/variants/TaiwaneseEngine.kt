package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object TaiwaneseEngine {

    fun validate(input: RoundInput, rules: ScoreRules.Taiwanese): ValidationError? {
        if (rules.base < 0 || rules.perTai < 0) return ValidationError.NegativeBase
        if (rules.dealerMultiplier < 1) return ValidationError.NegativeBase
        val tai = (input as? RoundInput.Tai)?.taiCount ?: return ValidationError.InputTypeMismatch
        if (tai < 1) return ValidationError.TaiBelowOne
        if (tai < rules.minTai) return ValidationError.TaiBelowMin
        return validateDiscarder(input)
    }

    fun calculate(input: RoundInput.Tai, rules: ScoreRules.Taiwanese): RoundResult {
        val effectiveTai = if (input.winType == WinType.SELF_DRAW) {
            input.taiCount + rules.selfDrawBonusTai
        } else {
            input.taiCount
        }
        val unit = rules.base + effectiveTai * rules.perTai

        val deltas = zeroDeltas()
        when (input.winType) {
            WinType.SELF_DRAW -> {
                var winnerGain = 0
                Seat.entries.forEach { seat ->
                    if (seat == input.winner) return@forEach
                    val involvesDealer = input.winner == input.dealer || seat == input.dealer
                    val payment = if (involvesDealer) unit * rules.dealerMultiplier else unit
                    deltas[seat] = -payment
                    winnerGain += payment
                }
                deltas[input.winner] = winnerGain
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                val involvesDealer = input.winner == input.dealer || discarder == input.dealer
                val payment = if (involvesDealer) unit * rules.dealerMultiplier else unit
                deltas[input.winner] = payment
                deltas[discarder] = -payment
            }
        }
        return buildResult(deltas, input)
    }
}
