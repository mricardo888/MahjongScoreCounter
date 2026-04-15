package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object SingaporeanEngine {

    fun validate(input: RoundInput, rules: ScoreRules.Singaporean): ValidationError? {
        if (rules.discardBase < 0 || rules.selfDrawBase < 0) return ValidationError.NegativeBase
        if (rules.flowerBonus < 0 || rules.animalBonus < 0) return ValidationError.NegativeBase
        val fan = input as? RoundInput.Fan ?: return ValidationError.InputTypeMismatch
        if (fan.fanCount < 1) return ValidationError.FanBelowOne
        if (fan.flowerCount < 0 || fan.animalCount < 0) return ValidationError.NegativeCount
        return validateDiscarder(input)
    }

    fun calculate(input: RoundInput.Fan, rules: ScoreRules.Singaporean): RoundResult {
        val bonus = input.flowerCount * rules.flowerBonus + input.animalCount * rules.animalBonus
        val deltas = zeroDeltas()
        when (input.winType) {
            WinType.SELF_DRAW -> {
                val perLoser = rules.selfDrawBase * input.fanCount + bonus
                Seat.entries.forEach { seat ->
                    deltas[seat] = if (seat == input.winner) perLoser * 3 else -perLoser
                }
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                val amount = rules.discardBase * input.fanCount + bonus
                deltas[input.winner] = amount
                deltas[discarder] = -amount
            }
        }
        return buildResult(deltas, input)
    }
}
