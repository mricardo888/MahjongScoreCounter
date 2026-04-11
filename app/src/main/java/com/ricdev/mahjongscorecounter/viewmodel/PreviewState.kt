package com.ricdev.mahjongscorecounter.viewmodel

import com.ricdev.mahjongscorecounter.model.RoundResult
import com.ricdev.mahjongscorecounter.model.ValidationError

sealed interface PreviewState {
    data object Empty : PreviewState
    data class Invalid(val error: ValidationError) : PreviewState
    data class Valid(val result: RoundResult) : PreviewState
}
