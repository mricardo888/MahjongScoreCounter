package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object HongKongNewEngine {

    fun validate(input: RoundInput, rules: ScoreRules.HongKongNew): ValidationError? {
        if (rules.discardBase < 0 || rules.selfDrawBase < 0) return ValidationError.NegativeBase
        val fan = (input as? RoundInput.Fan)?.fanCount ?: return ValidationError.InputTypeMismatch
        if (fan < 1) return ValidationError.FanBelowOne
        return validateDiscarder(input)
    }

    fun calculate(input: RoundInput.Fan, rules: ScoreRules.HongKongNew): RoundResult {
        val effFan = rules.maxFan?.let { minOf(input.fanCount, it) } ?: input.fanCount
        val deltas = zeroDeltas()
        when (input.winType) {
            WinType.SELF_DRAW -> {
                val perLoser = rules.selfDrawBase * effFan
                Seat.entries.forEach { seat ->
                    deltas[seat] = if (seat == input.winner) perLoser * 3 else -perLoser
                }
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                val amount = rules.discardBase * effFan
                deltas[input.winner] = amount
                deltas[discarder] = -amount
            }
        }
        return buildResult(deltas, input)
    }
}
