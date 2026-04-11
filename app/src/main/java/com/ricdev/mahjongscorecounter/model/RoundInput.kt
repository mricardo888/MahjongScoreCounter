package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundInput(
    @SerialName("winner") val winner: Seat,
    @SerialName("win_type") val winType: WinType,
    @SerialName("fan_count") val fanCount: Int,
    @SerialName("discarder") val discarder: Seat? = null,
)
