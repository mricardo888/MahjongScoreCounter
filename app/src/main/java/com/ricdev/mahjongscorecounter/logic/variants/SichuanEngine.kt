package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object SichuanEngine {

    fun validate(input: RoundInput, rules: ScoreRules.Sichuan): ValidationError? {
        if (rules.base < 0) return ValidationError.NegativeBase
        val fan = (input as? RoundInput.Fan)?.fanCount ?: return ValidationError.InputTypeMismatch
        if (fan < 1) return ValidationError.FanBelowOne
        return validateDiscarder(input)
    }

    fun calculate(input: RoundInput.Fan, rules: ScoreRules.Sichuan): RoundResult {
        val effFan = minOf(input.fanCount, rules.maxFan.coerceAtLeast(1))
        val amount = rules.base shl (effFan - 1)
        val deltas = zeroDeltas()
        when (input.winType) {
            WinType.SELF_DRAW -> {
                Seat.entries.forEach { seat ->
                    deltas[seat] = if (seat == input.winner) amount * 3 else -amount
                }
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                deltas[input.winner] = amount
                deltas[discarder] = -amount
            }
        }
        return buildResult(deltas, input)
    }
}
