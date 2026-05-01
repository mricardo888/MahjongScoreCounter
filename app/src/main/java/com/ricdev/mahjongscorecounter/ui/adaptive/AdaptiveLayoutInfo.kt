package com.ricdev.mahjongscorecounter.ui.adaptive

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

private const val WIDTH_DP_LARGE_LOWER_BOUND = 1200
private const val WIDTH_DP_EXTRA_LARGE_LOWER_BOUND = 1600

data class AdaptiveLayoutInfo(
    val widthSizeClass: AdaptiveWidthSizeClass,
    val heightSizeClass: AdaptiveHeightSizeClass,
) {
    val usesNavigationRail: Boolean
        get() = widthSizeClass >= AdaptiveWidthSizeClass.Medium &&
            heightSizeClass != AdaptiveHeightSizeClass.Compact

    val usesTwoPaneScoreTracker: Boolean
        get() = widthSizeClass >= AdaptiveWidthSizeClass.Expanded &&
            heightSizeClass != AdaptiveHeightSizeClass.Compact

    val usesRecentRoundsGrid: Boolean
        get() = widthSizeClass >= AdaptiveWidthSizeClass.Medium

    val usesWideSettingsLayout: Boolean
        get() = widthSizeClass >= AdaptiveWidthSizeClass.Expanded

    val constrainsSettingsWidth: Boolean
        get() = widthSizeClass >= AdaptiveWidthSizeClass.Medium

    companion object {
        fun from(windowSizeClass: WindowSizeClass): AdaptiveLayoutInfo {
            val widthSizeClass = when {
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) ->
                    AdaptiveWidthSizeClass.ExtraLarge
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) ->
                    AdaptiveWidthSizeClass.Large
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                    AdaptiveWidthSizeClass.Expanded
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                    AdaptiveWidthSizeClass.Medium
                else -> AdaptiveWidthSizeClass.Compact
            }
            val heightSizeClass = when {
                windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_EXPANDED_LOWER_BOUND) ->
                    AdaptiveHeightSizeClass.Expanded
                windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND) ->
                    AdaptiveHeightSizeClass.Medium
                else -> AdaptiveHeightSizeClass.Compact
            }
            return AdaptiveLayoutInfo(widthSizeClass, heightSizeClass)
        }
    }
}

enum class AdaptiveWidthSizeClass {
    Compact,
    Medium,
    Expanded,
    Large,
    ExtraLarge,
}

enum class AdaptiveHeightSizeClass {
    Compact,
    Medium,
    Expanded,
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun currentAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    return AdaptiveLayoutInfo.from(adaptiveInfo.windowSizeClass)
}
