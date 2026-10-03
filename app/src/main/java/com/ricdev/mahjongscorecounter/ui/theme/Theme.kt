package com.ricdev.mahjongscorecounter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class MahjongColors(
    val felt: Color,
    val feltLit: Color,
    val feltRim: Color,
    val inlay: Color,
    val winnerRing: Color,
    val tileFace: Color,
    val tileFaceShade: Color,
    val tileBack: Color,
    val tileBackShade: Color,
    val tileInk: Color,
    val tileDeltaPositive: Color,
    val tileDeltaNegative: Color,
    val dragonRed: Color,
    val deltaPositive: Color,
    val deltaNegative: Color,
    val deltaNeutral: Color,
)

private val LightMahjongColors = MahjongColors(
    felt = Felt,
    feltLit = FeltLit,
    feltRim = FeltRim,
    inlay = GoldInlay,
    winnerRing = GoldRing,
    tileFace = TileIvory,
    tileFaceShade = TileIvoryShade,
    tileBack = TileJadeBack,
    tileBackShade = TileJadeBackShade,
    tileInk = TileInk,
    tileDeltaPositive = TileDeltaPositive,
    tileDeltaNegative = TileDeltaNegative,
    dragonRed = DragonRed,
    deltaPositive = DeltaPositive,
    deltaNegative = DeltaNegative,
    deltaNeutral = DeltaNeutral,
)

private val DarkMahjongColors = MahjongColors(
    felt = FeltDark,
    feltLit = FeltLitDark,
    feltRim = FeltRimDark,
    inlay = GoldInlayDark,
    winnerRing = GoldRing,
    tileFace = TileIvoryDark,
    tileFaceShade = TileIvoryShadeDark,
    tileBack = TileJadeBackDark,
    tileBackShade = TileJadeBackShadeDark,
    tileInk = TileInk,
    tileDeltaPositive = TileDeltaPositive,
    tileDeltaNegative = TileDeltaNegative,
    dragonRed = DragonRed,
    deltaPositive = DeltaPositiveDark,
    deltaNegative = DeltaNegativeDark,
    deltaNeutral = DeltaNeutralDark,
)

val LocalMahjongColors = staticCompositionLocalOf { LightMahjongColors }

val MaterialTheme.mahjongColors: MahjongColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMahjongColors.current

private val LightColorScheme = lightColorScheme(
    primary = JadePrimary,
    onPrimary = JadeOnPrimary,
    primaryContainer = JadePrimaryContainer,
    onPrimaryContainer = JadeOnPrimaryContainer,
    secondary = JadeSecondary,
    onSecondary = JadeOnSecondary,
    secondaryContainer = JadeSecondaryContainer,
    onSecondaryContainer = JadeOnSecondaryContainer,
    tertiary = CinnabarTertiary,
    onTertiary = CinnabarOnTertiary,
    tertiaryContainer = CinnabarTertiaryContainer,
    onTertiaryContainer = CinnabarOnTertiaryContainer,
    background = CeladonBackground,
    onBackground = CeladonOnBackground,
    surface = CeladonBackground,
    onSurface = CeladonOnBackground,
    surfaceVariant = CeladonSurfaceVariant,
    onSurfaceVariant = CeladonOnSurfaceVariant,
    surfaceTint = JadePrimary,
    surfaceContainerLowest = CeladonContainerLowest,
    surfaceContainerLow = CeladonContainerLow,
    surfaceContainer = CeladonContainer,
    surfaceContainerHigh = CeladonContainerHigh,
    surfaceContainerHighest = CeladonContainerHighest,
    outline = CeladonOutline,
    outlineVariant = CeladonOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = JadePrimaryDark,
    onPrimary = JadeOnPrimaryDark,
    primaryContainer = JadePrimaryContainerDark,
    onPrimaryContainer = JadeOnPrimaryContainerDark,
    secondary = JadeSecondaryDark,
    onSecondary = JadeOnSecondaryDark,
    secondaryContainer = JadeSecondaryContainerDark,
    onSecondaryContainer = JadeOnSecondaryContainerDark,
    tertiary = CinnabarTertiaryDark,
    onTertiary = CinnabarOnTertiaryDark,
    tertiaryContainer = CinnabarTertiaryContainerDark,
    onTertiaryContainer = CinnabarOnTertiaryContainerDark,
    background = CeladonBackgroundDark,
    onBackground = CeladonOnBackgroundDark,
    surface = CeladonBackgroundDark,
    onSurface = CeladonOnBackgroundDark,
    surfaceVariant = CeladonSurfaceVariantDark,
    onSurfaceVariant = CeladonOnSurfaceVariantDark,
    surfaceTint = JadePrimaryDark,
    surfaceContainerLowest = CeladonContainerLowestDark,
    surfaceContainerLow = CeladonContainerLowDark,
    surfaceContainer = CeladonContainerDark,
    surfaceContainerHigh = CeladonContainerHighDark,
    surfaceContainerHighest = CeladonContainerHighestDark,
    outline = CeladonOutlineDark,
    outlineVariant = CeladonOutlineVariantDark,
)

private val MahjongShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MahjongScoreCounterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalMahjongColors provides if (darkTheme) DarkMahjongColors else LightMahjongColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = MahjongShapes,
            content = content,
        )
    }
}
