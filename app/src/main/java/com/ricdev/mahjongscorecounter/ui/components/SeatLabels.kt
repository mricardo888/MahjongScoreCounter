package com.ricdev.mahjongscorecounter.ui.components

import com.ricdev.mahjongscorecounter.R
import com.ricdev.mahjongscorecounter.model.Seat
import java.text.NumberFormat

internal fun Seat.labelResId(): Int = when (this) {
    Seat.EAST -> R.string.seat_east
    Seat.SOUTH -> R.string.seat_south
    Seat.WEST -> R.string.seat_west
    Seat.NORTH -> R.string.seat_north
}

internal fun Seat.shortLabelResId(): Int = when (this) {
    Seat.EAST -> R.string.seat_east_short
    Seat.SOUTH -> R.string.seat_south_short
    Seat.WEST -> R.string.seat_west_short
    Seat.NORTH -> R.string.seat_north_short
}

/** The character printed on the wind tile. Tile markings are the same in every locale. */
internal fun Seat.windGlyph(): String = when (this) {
    Seat.EAST -> "東"
    Seat.SOUTH -> "南"
    Seat.WEST -> "西"
    Seat.NORTH -> "北"
}

/** True when the localized seat name is just the tile character (zh, ja), so showing both is redundant. */
internal fun Seat.isNamedByGlyph(label: String): Boolean =
    label == windGlyph() || (this == Seat.EAST && label == "东")

private const val MINUS_SIGN = '−'

/** Display formatting: a true minus sign, which matches the width of "+" in tabular figures. */
internal fun NumberFormat.formatScore(value: Int): String = format(value).replace('-', MINUS_SIGN)

internal fun NumberFormat.formatDelta(value: Int): String =
    if (value > 0) "+${format(value)}" else formatScore(value)
