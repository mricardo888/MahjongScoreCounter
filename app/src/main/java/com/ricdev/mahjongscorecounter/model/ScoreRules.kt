package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScoreRules(
    @SerialName("self_draw_base") val selfDrawBase: Int = 4,
    @SerialName("discard_win_base") val discardWinBase: Int = 8,
)
