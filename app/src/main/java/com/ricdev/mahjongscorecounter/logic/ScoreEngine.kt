package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object ScoreEngine {

    /** Computes the per-player amount for a given base and fan count: base * 2^(fan - 1). */
    fun amountForFan(base: Int, fan: Int): Int {
        require(base >= 0) { "base must be non-negative" }
        require(fan >= 1) { "fan must be >= 1" }
        return base shl (fan - 1)
    }

    /** Validates [input] against [rules]. Returns null when the input is valid. */
    fun validate(input: RoundInput, rules: ScoreRules): ValidationError? {
        if (rules.selfDrawBase < 0 || rules.discardWinBase < 0) {
            return ValidationError.NegativeBase
        }
        if (input.fanCount < 1) {
            return ValidationError.FanBelowOne
        }
        when (input.winType) {
            WinType.SELF_DRAW -> {
                if (input.discarder != null) return ValidationError.DiscarderForbiddenForSelfDraw
            }
            WinType.DISCARD_WIN -> {
                if (input.discarder == null) return ValidationError.DiscarderRequired
                if (input.discarder == input.winner) return ValidationError.WinnerIsDiscarder
            }
        }
        return null
    }

    /**
     * Runs the scoring formulas for a validated input. Callers must ensure [validate]
     * returns null beforehand; this function throws IllegalArgumentException otherwise.
     */
    fun calculate(input: RoundInput, rules: ScoreRules): RoundResult {
        val error = validate(input, rules)
        require(error == null) { "Invalid round input: $error" }

        val deltas: MutableMap<Seat, Int> = Seat.entries.associateWith { 0 }.toMutableMap()
        val perLoser: Int
        val total: Int
        when (input.winType) {
            WinType.SELF_DRAW -> {
                perLoser = amountForFan(rules.selfDrawBase, input.fanCount)
                total = perLoser * 3
                Seat.entries.forEach { seat ->
                    if (seat == input.winner) {
                        deltas[seat] = total
                    } else {
                        deltas[seat] = -perLoser
                    }
                }
            }
            WinType.DISCARD_WIN -> {
                val discarder = requireNotNull(input.discarder)
                perLoser = amountForFan(rules.discardWinBase, input.fanCount)
                total = perLoser
                deltas[input.winner] = perLoser
                deltas[discarder] = -perLoser
            }
        }

        val sum = deltas.values.sum()
        check(sum == 0) { "Deltas must sum to zero, got $sum" }

        return RoundResult(
            deltas = deltas.toMap(),
            winner = input.winner,
            winType = input.winType,
            fanCount = input.fanCount,
            perLoserAmount = perLoser,
            totalAmount = total,
        )
    }

    /** Derives per-seat totals by summing deltas across committed history. */
    fun computeTotals(history: List<CommittedRound>): Map<Seat, Int> {
        val totals: MutableMap<Seat, Int> = Seat.entries.associateWith { 0 }.toMutableMap()
        history.forEach { round ->
            round.result.deltas.forEach { (seat, delta) ->
                totals[seat] = (totals[seat] ?: 0) + delta
            }
        }
        return totals.toMap()
    }
}
