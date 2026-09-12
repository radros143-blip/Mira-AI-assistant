package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MyraBlack
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed


@Composable
fun BackgroundRgbGlow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) {
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRect(color = MyraBlack)
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "BackgroundGlowTransition")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BgPhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        // Base deep obsidian black
        drawRect(color = MyraBlack)

        // Subtle animation pulse
        val pulse = 1f + 0.1f * kotlin.math.sin(phase * 2f * Math.PI.toFloat())

        // Top-left 1/4th Blue ambient glow
        val cx1 = w * 0.25f
        val cy1 = h * 0.25f
        val r1 = (w * 0.75f) * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonBlue.copy(alpha = 0.22f),
                    NeonCyan.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(cx1, cy1),
                radius = r1
            ),
            center = Offset(cx1, cy1),
            radius = r1
        )

        // Bottom-right 1/4th Purple ambient glow
        val cx2 = w * 0.75f
        val cy2 = h * 0.75f
        val r2 = (w * 0.75f) * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonPurple.copy(alpha = 0.22f),
                    NeonMagenta.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(cx2, cy2),
                radius = r2
            ),
            center = Offset(cx2, cy2),
            radius = r2
        )

    }
}
