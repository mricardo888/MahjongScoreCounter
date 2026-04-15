package com.ricdev.mahjongscorecounter.logic.variants

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError
import com.ricdev.mahjongscorecounter.model.WinType

internal fun validateDiscarder(input: RoundInput): ValidationError? {
    return when (input.winType) {
        WinType.SELF_DRAW ->
            if (input.discarder != null) ValidationError.DiscarderForbiddenForSelfDraw else null
        WinType.DISCARD_WIN -> {
            if (input.discarder == null) return ValidationError.DiscarderRequired
            if (input.discarder == input.winner) return ValidationError.WinnerIsDiscarder
            null
        }
    }
}

internal fun zeroDeltas(): MutableMap<Seat, Int> =
    Seat.entries.associateWith { 0 }.toMutableMap()

internal fun buildResult(
    deltas: Map<Seat, Int>,
    input: RoundInput,
): RoundResult {
    val sum = deltas.values.sum()
    check(sum == 0) { "Deltas must sum to zero, got $sum" }
    return RoundResult(
        deltas = deltas,
        winner = input.winner,
        winType = input.winType,
    )
}
