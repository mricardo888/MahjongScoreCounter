package com.ricdev.mahjongscorecounter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ricdev.mahjongscorecounter.R

@OptIn(ExperimentalTextApi::class)
private fun bitter(weight: Int) = Font(
    resId = R.font.bitter,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

// Slab serif for scores and titles — the numbers people read from across the table.
val Bitter = FontFamily(
    bitter(500),
    bitter(600),
    bitter(700),
    bitter(800),
)

// Calligraphic 東南西北中, subset from LXGW WenKai TC. Only used for tile faces.
val TileGlyphs = FontFamily(Font(R.font.tile_glyphs, FontWeight.Bold))

// Tabular, lining figures so columns of scores line up.
val ScoreNumerals = TextStyle(
    fontFamily = Bitter,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum, lnum",
)

private val Base = Typography()

val Typography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontFamily = Bitter, fontWeight = FontWeight.Bold),
    displayMedium = Base.displayMedium.copy(fontFamily = Bitter, fontWeight = FontWeight.Bold),
    displaySmall = Base.displaySmall.copy(fontFamily = Bitter, fontWeight = FontWeight.Bold),
    headlineLarge = Base.headlineLarge.copy(fontFamily = Bitter, fontWeight = FontWeight.SemiBold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = Bitter, fontWeight = FontWeight.SemiBold),
    headlineSmall = Base.headlineSmall.copy(fontFamily = Bitter, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(
        fontFamily = Bitter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)
