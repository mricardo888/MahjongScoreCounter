package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommittedRound(
    @SerialName("input") val input: RoundInput,
    @SerialName("result") val result: RoundResult,
    @SerialName("timestamp_millis") val timestampMillis: Long,
)
