package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface RoundInput {
    val winner: Seat
    val winType: WinType
    val discarder: Seat?

    @Serializable
    @SerialName("fan")
    data class Fan(
        @SerialName("winner") override val winner: Seat,
        @SerialName("win_type") override val winType: WinType,
        @SerialName("discarder") override val discarder: Seat? = null,
        @SerialName("fan_count") val fanCount: Int,
        @SerialName("flower_count") val flowerCount: Int = 0,
        @SerialName("animal_count") val animalCount: Int = 0,
    ) : RoundInput

    @Serializable
    @SerialName("tai")
    data class Tai(
        @SerialName("winner") override val winner: Seat,
        @SerialName("win_type") override val winType: WinType,
        @SerialName("discarder") override val discarder: Seat? = null,
        @SerialName("tai_count") val taiCount: Int,
        @SerialName("dealer") val dealer: Seat = Seat.EAST,
    ) : RoundInput

    @Serializable
    @SerialName("riichi")
    data class Riichi(
        @SerialName("winner") override val winner: Seat,
        @SerialName("win_type") override val winType: WinType,
        @SerialName("discarder") override val discarder: Seat? = null,
        @SerialName("han") val han: Int,
        @SerialName("fu") val fu: Int,
        @SerialName("dealer") val dealer: Seat = Seat.EAST,
        @SerialName("honba_count") val honbaCount: Int = 0,
        @SerialName("riichi_sticks") val riichiSticks: Int = 0,
    ) : RoundInput
}
