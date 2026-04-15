package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundResult(
    @SerialName("deltas") val deltas: Map<Seat, Int>,
    @SerialName("winner") val winner: Seat,
    @SerialName("win_type") val winType: WinType,
    @SerialName("stick_bonus") val stickBonus: Int = 0,
) {
    val winnerGain: Int get() = (deltas[winner] ?: 0) + stickBonus
    val largestLoss: Int get() = -(deltas.values.minOrNull() ?: 0)
}
