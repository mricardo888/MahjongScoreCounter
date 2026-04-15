package com.ricdev.mahjongscorecounter.logic

import com.ricdev.mahjongscorecounter.logic.variants.HokkienEngine
import com.ricdev.mahjongscorecounter.logic.variants.HongKongNewEngine
import com.ricdev.mahjongscorecounter.logic.variants.RiichiEngine
import com.ricdev.mahjongscorecounter.logic.variants.ShanghaiEngine
import com.ricdev.mahjongscorecounter.logic.variants.SichuanEngine
import com.ricdev.mahjongscorecounter.logic.variants.SingaporeanEngine
import com.ricdev.mahjongscorecounter.logic.variants.TaiwaneseEngine
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ValidationError

object ScoreEngine {

    fun validate(input: RoundInput, rules: ScoreRules): ValidationError? = when (rules) {
        is ScoreRules.HongKongNew -> HongKongNewEngine.validate(input, rules)
        is ScoreRules.Taiwanese -> TaiwaneseEngine.validate(input, rules)
        is ScoreRules.JapaneseRiichi -> RiichiEngine.validate(input, rules)
        is ScoreRules.Hokkien -> HokkienEngine.validate(input, rules)
        is ScoreRules.Shanghai -> ShanghaiEngine.validate(input, rules)
        is ScoreRules.Sichuan -> SichuanEngine.validate(input, rules)
        is ScoreRules.Singaporean -> SingaporeanEngine.validate(input, rules)
    }

    fun calculate(input: RoundInput, rules: ScoreRules): RoundResult {
        val error = validate(input, rules)
        require(error == null) { "Invalid round input: $error" }
        return when (rules) {
            is ScoreRules.HongKongNew -> HongKongNewEngine.calculate(input as RoundInput.Fan, rules)
            is ScoreRules.Taiwanese -> TaiwaneseEngine.calculate(input as RoundInput.Tai, rules)
            is ScoreRules.JapaneseRiichi -> RiichiEngine.calculate(input as RoundInput.Riichi, rules)
            is ScoreRules.Hokkien -> HokkienEngine.calculate(input as RoundInput.Tai, rules)
            is ScoreRules.Shanghai -> ShanghaiEngine.calculate(input as RoundInput.Fan, rules)
            is ScoreRules.Sichuan -> SichuanEngine.calculate(input as RoundInput.Fan, rules)
            is ScoreRules.Singaporean -> SingaporeanEngine.calculate(input as RoundInput.Fan, rules)
        }
    }

    fun computeTotals(history: List<CommittedRound>): Map<Seat, Int> {
        val totals = Seat.entries.associateWith { 0 }.toMutableMap()
        history.forEach { round ->
            round.result.deltas.forEach { (seat, delta) ->
                totals[seat] = (totals[seat] ?: 0) + delta
            }
            if (round.result.stickBonus != 0) {
                val winner = round.result.winner
                totals[winner] = (totals[winner] ?: 0) + round.result.stickBonus
            }
        }
        return totals.toMap()
    }
}
