package com.bigcash.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.theme.LocalGlassTokens

/** Full screen backdrop: deep glass with slow moving light blobs behind every screen. */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val transition = rememberInfiniteTransition(label = "liquid")
    val driftX by transition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(14_000), RepeatMode.Reverse),
        label = "driftX",
    )
    val driftY by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = -0.12f,
        animationSpec = infiniteRepeatable(tween(18_000), RepeatMode.Reverse),
        label = "driftY",
    )

    Box(modifier = modifier.background(tokens.backgroundBrush)) {
        LightBlob(
            color = GlassColors.Mint,
            alpha = if (tokens.isDark) 0.22f else 0.28f,
            offsetX = driftX,
            offsetY = driftY,
            size = 320.dp,
        )
        LightBlob(
            color = GlassColors.Periwinkle,
            alpha = if (tokens.isDark) 0.20f else 0.24f,
            offsetX = -driftY,
            offsetY = driftX,
            size = 300.dp,
            alignRight = true,
        )
        LightBlob(
            color = GlassColors.Pink,
            alpha = if (tokens.isDark) 0.14f else 0.18f,
            offsetX = driftY,
            offsetY = -driftX,
            size = 260.dp,
            alignBottom = true,
        )
        content()
    }
}

@Composable
private fun BoxScope.LightBlob(
    color: Color,
    alpha: Float,
    offsetX: Float,
    offsetY: Float,
    size: Dp,
    alignRight: Boolean = false,
    alignBottom: Boolean = false,
) {
    val alignment = when {
        alignRight && alignBottom -> Alignment.BottomEnd
        alignRight -> Alignment.TopEnd
        alignBottom -> Alignment.BottomStart
        else -> Alignment.TopStart
    }
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = (offsetX * 220).dp, y = (offsetY * 220).dp)
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                ),
                shape = RoundedCornerShape(percent = 50),
            ),
    )
}

/** The core glassmorphism surface: translucent fill, hairline highlight border and soft depth. */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val haptics = LocalHapticFeedback.current
    val fillTop = if (tokens.isDark) 0.12f else 0.72f
    val fillBottom = if (tokens.isDark) 0.04f else 0.42f

    val clickModifier = remember(onClick) {
        if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        }
    }

    Column(
        modifier = modifier
            .shadow(
                elevation = if (tokens.isDark) 0.dp else 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.2f),
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        tokens.surfaceTint.copy(alpha = fillTop),
                        tokens.surfaceTint.copy(alpha = fillBottom),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        tokens.borderColor,
                        Color.Transparent,
                        tokens.borderColor.copy(alpha = 0.5f),
                    ),
                ),
                shape = shape,
            )
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}
