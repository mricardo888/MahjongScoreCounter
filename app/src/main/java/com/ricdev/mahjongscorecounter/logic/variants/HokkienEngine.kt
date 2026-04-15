package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object HokkienEngine {

    fun validate(input: RoundInput, rules: ScoreRules.Hokkien): ValidationError? {
        if (rules.base < 0 || rules.perUnit < 0) return ValidationError.NegativeBase
        val tai = (input as? RoundInput.Tai)?.taiCount ?: return ValidationError.InputTypeMismatch
        if (tai < 1) return ValidationError.TaiBelowOne
        return validateDiscarder(input)
    }

    fun calculate(input: RoundInput.Tai, rules: ScoreRules.Hokkien): RoundResult {
        val cappedTai = rules.maxUnits?.let { minOf(input.taiCount, it) } ?: input.taiCount
        val unit = rules.base + cappedTai * rules.perUnit
        val multiplier = if (rules.dealerDoubles) 2 else 1

        val deltas = zeroDeltas()
        when (input.winType) {
            WinType.SELF_DRAW -> {
                var winnerGain = 0
                Seat.entries.forEach { seat ->
                    if (seat == input.winner) return@forEach
                    val involvesDealer = input.winner == input.dealer || seat == input.dealer
                    val payment = if (involvesDealer) unit * multiplier else unit
                    deltas[seat] = -payment
                    winnerGain += payment
                }
                deltas[input.winner] = winnerGain
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                val involvesDealer = input.winner == input.dealer || discarder == input.dealer
                val payment = if (involvesDealer) unit * multiplier else unit
                deltas[input.winner] = payment
                deltas[discarder] = -payment
            }
        }
        return buildResult(deltas, input)
    }
}
