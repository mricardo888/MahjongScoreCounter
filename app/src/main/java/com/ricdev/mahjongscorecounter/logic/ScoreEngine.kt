package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

object ScoreEngine {
    const val MAX_ROUND_AMOUNT = 999_999

    fun validate(input: RoundInput): ValidationError? {
        if (input.amount <= 0) return ValidationError.AmountBelowOne
        if (input.amount > MAX_ROUND_AMOUNT) return ValidationError.AmountAboveMaximum

        return when (input.winType) {
            WinType.SELF_DRAW -> {
                if (input.payer != null) ValidationError.PayerForbiddenForSelfDraw else null
            }
            WinType.DISCARD_WIN -> {
                val payer = input.payer ?: return ValidationError.PayerRequired
                if (payer == input.winner) ValidationError.WinnerIsPayer else null
            }
        }
    }

    fun calculate(input: RoundInput): RoundResult {
        val error = validate(input)
        require(error == null) { "Invalid round input: $error" }
        val deltas = Seat.entries.associateWith { 0 }.toMutableMap()

        when (input.winType) {
            WinType.SELF_DRAW -> {
                Seat.entries.filterNot { it == input.winner }.forEach { seat ->
                    deltas[seat] = -input.amount
                }
                deltas[input.winner] = input.amount * (Seat.entries.size - 1)
            }
            WinType.DISCARD_WIN -> {
                val payer = requireNotNull(input.payer)
                deltas[payer] = -input.amount
                deltas[input.winner] = input.amount
            }
        }

        return RoundResult(
            deltas = deltas,
            winner = input.winner,
            winType = input.winType,
        )
    }

    fun computeTotals(history: List<CommittedRound>): Map<Seat, Int> {
        val totals = Seat.entries.associateWith { 0 }.toMutableMap()
        history.forEach { round ->
            round.result.deltas.forEach { (seat, delta) ->
                totals[seat] = (totals[seat] ?: 0) + delta
            }
        }
        return totals.toMap()
    }
}
