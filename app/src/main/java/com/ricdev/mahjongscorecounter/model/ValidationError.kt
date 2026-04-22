package com.ricdev.mahjongscorecounter.model

sealed interface ValidationError {
    data object AmountBelowOne : ValidationError
    data object PayerRequired : ValidationError
    data object PayerForbiddenForSelfDraw : ValidationError
    data object WinnerIsPayer : ValidationError
}
