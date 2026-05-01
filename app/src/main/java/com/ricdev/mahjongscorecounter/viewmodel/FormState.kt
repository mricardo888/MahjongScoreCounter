package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType

data class FormState(
    val winner: Seat? = null,
    val winType: WinType = WinType.SELF_DRAW,
    val payer: Seat? = null,
    val amountText: String = "",
)

// Clears payer if it matches the new winner (a player can't pay themselves).
fun FormState.withWinner(seat: Seat): FormState {
    val clearedPayer = if (payer == seat) null else payer
    return copy(winner = seat, payer = clearedPayer)
}

// Clears payer when switching to SELF_DRAW (no payer concept for self-draw).
fun FormState.withWinType(type: WinType): FormState {
    val clearedPayer = if (type == WinType.SELF_DRAW) null else payer
    return copy(winType = type, payer = clearedPayer)
}

// No-op if seat matches winner; a player cannot be their own payer.
fun FormState.withPayer(seat: Seat?): FormState {
    if (seat != null && seat == winner) return this
    return copy(payer = seat)
}
