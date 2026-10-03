package com.ricdev.mahjongscorecounter.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme
import com.ricdev.mahjongscorecounter.ui.theme.TileGlyphs
import com.ricdev.mahjongscorecounter.ui.theme.mahjongColors

/**
 * A mahjong tile seen from slightly above: an ivory face resting on a jade back, with the back
 * showing as a strip along the bottom edge. A lifted tile rises by its own depth and gains a ring.
 *
 * [faceModifier] is applied to the face after it is clipped, so click ripples follow the tile shape.
 */
@Composable
fun MahjongTile(
    modifier: Modifier = Modifier,
    depth: Dp = 5.dp,
    cornerRadius: Dp = 10.dp,
    lifted: Boolean = false,
    ringColor: Color = MaterialTheme.mahjongColors.winnerRing,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    faceModifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.mahjongColors
    val shape = RoundedCornerShape(cornerRadius)
    // Animated values are only read inside layer/draw lambdas, so a lift animates
    // without recomposing the tile (or re-measuring its text) on every frame.
    val lift by animateDpAsState(if (lifted) depth else 0.dp, label = "tile_lift")
    val ringAlpha by animateFloatAsState(if (lifted) 1f else 0f, label = "tile_ring")
    val elevation by animateDpAsState(if (lifted) 10.dp else 2.dp, label = "tile_elevation")

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = -lift.toPx()
                shadowElevation = elevation.toPx()
                this.shape = shape
                clip = false
            }
            .drawWithContent {
                drawContent()
                if (ringAlpha > 0f) {
                    val gap = 3.dp.toPx()
                    val stroke = 2.5.dp.toPx()
                    val inset = gap + stroke / 2f
                    drawRoundRect(
                        color = ringColor.copy(alpha = ringAlpha),
                        topLeft = Offset(-inset, -inset),
                        size = Size(size.width + inset * 2, size.height + inset * 2),
                        cornerRadius = CornerRadius(cornerRadius.toPx() + inset),
                        style = Stroke(width = stroke),
                    )
                }
            },
        propagateMinConstraints = true,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(listOf(colors.tileBack, colors.tileBackShade)), shape),
        )
        Box(
            modifier = Modifier
                .padding(bottom = depth)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(colors.tileFace, colors.tileFaceShade)))
                .then(faceModifier)
                .border(1.dp, Color.White.copy(alpha = 0.55f), shape)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

/** A character carved into a tile face. Hidden from accessibility services; callers describe the tile. */
@Composable
fun TileGlyph(
    glyph: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.mahjongColors.tileInk,
) {
    Text(
        text = glyph,
        modifier = modifier.clearAndSetSemantics {},
        color = color,
        style = TextStyle(
            fontFamily = TileGlyphs,
            fontSize = fontSize,
            lineHeight = 1.1.em,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        ),
    )
}

/** The red dragon tile from the app icon, used as the brand mark in the top bar. */
@Composable
fun BrandTileMark(modifier: Modifier = Modifier) {
    MahjongTile(
        modifier = modifier.size(width = 28.dp, height = 34.dp),
        depth = 4.dp,
        cornerRadius = 6.dp,
    ) {
        TileGlyph(
            glyph = "中",
            fontSize = 19.sp,
            color = MaterialTheme.mahjongColors.dragonRed,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF145C42)
@Composable
private fun MahjongTilePreview() {
    MahjongScoreCounterTheme {
        Box(Modifier.padding(16.dp)) {
            MahjongTile(modifier = Modifier.size(56.dp, 72.dp), lifted = true) {
                TileGlyph(glyph = "東", fontSize = 30.sp)
            }
        }
    }
}
