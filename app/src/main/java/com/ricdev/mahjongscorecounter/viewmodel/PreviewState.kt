package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ValidationError

sealed interface PreviewState {
    data object Empty : PreviewState
    data class Invalid(val error: ValidationError) : PreviewState
    data class Valid(val input: RoundInput, val result: RoundResult) : PreviewState
}
