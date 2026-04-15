package com.ricdev.mahjongscorecounter.model

import com.ricdev.mahjongscorecounter.logic.ScoreEngine

data class GameState(
    val rules: ScoreRules = ScoreRules.HongKongNew(),
    val history: List<CommittedRound> = emptyList(),
    val dealer: Seat = Seat.EAST,
    val honbaCount: Int = 0,
    val riichiSticksOnTable: Int = 0,
) {
    val totals: Map<Seat, Int>
        get() = ScoreEngine.computeTotals(history)

    val variant: MahjongVariant
        get() = rules.variant()
}
