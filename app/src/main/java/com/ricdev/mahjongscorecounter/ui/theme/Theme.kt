package com.ricdev.mahjongscorecounter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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

data class MahjongColors(
    val tableFelt: Color,
    val tableWood: Color,
    val deltaPositive: Color,
    val deltaNegative: Color,
    val deltaNeutral: Color,
)

private val LightMahjongColors = MahjongColors(
    tableFelt = FeltGreen,
    tableWood = WoodBrown,
    deltaPositive = DeltaPositive,
    deltaNegative = DeltaNegative,
    deltaNeutral = DeltaNeutral,
)

private val DarkMahjongColors = MahjongColors(
    tableFelt = FeltGreenDark,
    tableWood = WoodBrownDark,
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
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = IndigoOnPrimaryContainer,
    secondary = IndigoSecondary,
    onSecondary = IndigoOnSecondary,
    secondaryContainer = IndigoSecondaryContainer,
    onSecondaryContainer = IndigoOnSecondaryContainer,
    tertiary = IndigoTertiary,
    onTertiary = IndigoOnTertiary,
    background = IndigoBackground,
    onBackground = IndigoOnBackground,
    surface = IndigoSurface,
    onSurface = IndigoOnSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = IndigoPrimaryContainerDark,
    onPrimaryContainer = IndigoOnPrimaryContainerDark,
    secondary = IndigoSecondaryDark,
    onSecondary = IndigoOnSecondaryDark,
    secondaryContainer = IndigoSecondaryContainerDark,
    onSecondaryContainer = IndigoOnSecondaryContainerDark,
    tertiary = IndigoTertiaryDark,
    onTertiary = IndigoOnTertiaryDark,
    background = IndigoBackgroundDark,
    onBackground = IndigoOnBackgroundDark,
    surface = IndigoSurfaceDark,
    onSurface = IndigoOnSurfaceDark,
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
            content = content,
        )
    }
}
