package com.ricdev.mahjongscorecounter.model

sealed interface ValidationError {
    data object NoWinner : ValidationError
    data object FanBelowOne : ValidationError
    data object DiscarderRequired : ValidationError
    data object DiscarderForbiddenForSelfDraw : ValidationError
    data object WinnerIsDiscarder : ValidationError
    data object NegativeBase : ValidationError

    data object TaiBelowOne : ValidationError
    data object TaiBelowMin : ValidationError
    data object HanBelowOne : ValidationError
    data object FuBelowMin : ValidationError
    data object FuInvalid : ValidationError
    data object InputTypeMismatch : ValidationError
    data object NegativeCount : ValidationError
}
