package com.ricdev.mahjongscorecounter.model

sealed interface ValidationError {
    data object NoWinner : ValidationError
    data object FanBelowOne : ValidationError
    data object DiscarderRequired : ValidationError
    data object DiscarderForbiddenForSelfDraw : ValidationError
    data object WinnerIsDiscarder : ValidationError
    data object NegativeBase : ValidationError
}
