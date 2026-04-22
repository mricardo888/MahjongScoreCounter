package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType

data class FormState(
    val winner: Seat? = null,
    val winType: WinType = WinType.SELF_DRAW,
    val payer: Seat? = null,
    val amount: Int = 0,
)

fun FormState.withWinner(seat: Seat): FormState {
    val clearedPayer = if (payer == seat) null else payer
    return copy(winner = seat, payer = clearedPayer)
}

fun FormState.withWinType(type: WinType): FormState {
    val clearedPayer = if (type == WinType.SELF_DRAW) null else payer
    return copy(winType = type, payer = clearedPayer)
}

fun FormState.withPayer(seat: Seat?): FormState {
    if (seat != null && seat == winner) return this
    return copy(payer = seat)
}
