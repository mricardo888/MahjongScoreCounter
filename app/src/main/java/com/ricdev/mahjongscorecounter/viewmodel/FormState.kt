package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.MahjongVariant
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType

sealed interface FormState {
    val winner: Seat?
    val winType: WinType
    val discarder: Seat?

    data class Fan(
        override val winner: Seat? = null,
        override val winType: WinType = WinType.SELF_DRAW,
        override val discarder: Seat? = null,
        val fanCount: Int = 1,
        val flowerCount: Int = 0,
        val animalCount: Int = 0,
    ) : FormState

    data class Tai(
        override val winner: Seat? = null,
        override val winType: WinType = WinType.SELF_DRAW,
        override val discarder: Seat? = null,
        val taiCount: Int = 5,
    ) : FormState

    data class Riichi(
        override val winner: Seat? = null,
        override val winType: WinType = WinType.SELF_DRAW,
        override val discarder: Seat? = null,
        val han: Int = 1,
        val fu: Int = 30,
    ) : FormState

    companion object {
        fun defaultFor(variant: MahjongVariant): FormState = when (variant) {
            MahjongVariant.HONG_KONG_NEW,
            MahjongVariant.SHANGHAI,
            MahjongVariant.SICHUAN,
            MahjongVariant.SINGAPOREAN -> Fan()
            MahjongVariant.TAIWANESE -> Tai(taiCount = 5)
            MahjongVariant.HOKKIEN -> Tai(taiCount = 1)
            MahjongVariant.JAPANESE_RIICHI -> Riichi()
        }
    }
}

fun FormState.withWinner(seat: Seat): FormState {
    val clearedDiscarder: Seat? = if (discarder == seat) null else discarder
    return when (this) {
        is FormState.Fan -> copy(winner = seat, discarder = clearedDiscarder)
        is FormState.Tai -> copy(winner = seat, discarder = clearedDiscarder)
        is FormState.Riichi -> copy(winner = seat, discarder = clearedDiscarder)
    }
}

fun FormState.withWinType(type: WinType): FormState {
    val clearedDiscarder: Seat? = if (type == WinType.SELF_DRAW) null else discarder
    return when (this) {
        is FormState.Fan -> copy(winType = type, discarder = clearedDiscarder)
        is FormState.Tai -> copy(winType = type, discarder = clearedDiscarder)
        is FormState.Riichi -> copy(winType = type, discarder = clearedDiscarder)
    }
}

fun FormState.withDiscarder(seat: Seat?): FormState {
    if (seat != null && seat == winner) return this
    return when (this) {
        is FormState.Fan -> copy(discarder = seat)
        is FormState.Tai -> copy(discarder = seat)
        is FormState.Riichi -> copy(discarder = seat)
    }
}
