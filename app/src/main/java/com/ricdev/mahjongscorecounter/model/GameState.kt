package com.ricdev.mahjongscorecounter.model

import com.ricdev.mahjongscorecounter.logic.ScoreEngine

data class GameState(
    val rules: ScoreRules = ScoreRules(),
    val history: List<CommittedRound> = emptyList(),
) {
    val totals: Map<Seat, Int>
        get() = ScoreEngine.computeTotals(history)
}
