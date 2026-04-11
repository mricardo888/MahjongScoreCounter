package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType

data class FormState(
    val winner: Seat? = null,
    val winType: WinType = WinType.SELF_DRAW,
    val fanCount: Int = 1,
    val discarder: Seat? = null,
)
