package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundResult(
    @SerialName("deltas") val deltas: Map<Seat, Int>,
    @SerialName("winner") val winner: Seat,
    @SerialName("win_type") val winType: WinType,
    @SerialName("fan_count") val fanCount: Int,
    @SerialName("per_loser_amount") val perLoserAmount: Int,
    @SerialName("total_amount") val totalAmount: Int,
)
