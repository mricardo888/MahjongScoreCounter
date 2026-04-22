package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundInput(
    @SerialName("winner") val winner: Seat,
    @SerialName("win_type") val winType: WinType,
    @SerialName("payer") val payer: Seat? = null,
    @SerialName("amount") val amount: Int,
)
