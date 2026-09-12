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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.RgbBrightness
import com.example.data.RgbSpeed
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SuccessGreen

enum class RgbState {
    IDLE,
    NORMAL,
    LISTENING,
    PROCESSING,
    SUCCESS,
    ERROR
}

val RgbSpectrumList = listOf(
    NeonRed,
    NeonOrange,
    NeonYellow,
    NeonGreen,
    NeonCyan,
    NeonBlue,
    NeonPurple,
    NeonMagenta,
    NeonRed
)

@Composable
fun RgbEdgeLight(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    speed: RgbSpeed = RgbSpeed.NORMAL,
    brightness: RgbBrightness = RgbBrightness.MEDIUM,
    state: RgbState = RgbState.NORMAL
) {
    if (!enabled) return

    val baseDuration = when (state) {
        RgbState.LISTENING -> (speed.durationMs * 0.6f).toInt()
        RgbState.PROCESSING -> (speed.durationMs * 0.4f).toInt()
        RgbState.SUCCESS, RgbState.ERROR -> 1500
        else -> speed.durationMs
    }.coerceAtLeast(1000)

    val transition = rememberInfiniteTransition(label = "RgbEdgeLightTransition")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = baseDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RgbEdgeProgress"
    )

    val pulseScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RgbPulseScale"
    )

    val effectiveAlpha = when (state) {
        RgbState.LISTENING -> (brightness.alphaFactor * 1.15f).coerceAtMost(1f)
        RgbState.PROCESSING -> (brightness.alphaFactor * 1.1f).coerceAtMost(1f)
        RgbState.SUCCESS, RgbState.ERROR -> 1.0f
        else -> brightness.alphaFactor
    }

    val strokeWidthPx = brightness.strokeWidthDp.dp

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val perimeter = 2 * (width + height)
        val stroke = strokeWidthPx.toPx()
        val halfStroke = stroke / 2f

        // Draw State-specific static or running glow
        when (state) {
            RgbState.SUCCESS -> {
                val successColor = SuccessGreen.copy(alpha = effectiveAlpha)
                drawRect(
                    color = successColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(width - stroke, height - stroke),
                    style = Stroke(width = stroke * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                return@Canvas
            }
            RgbState.ERROR -> {
                val errorColor = ErrorRed.copy(alpha = effectiveAlpha)
                drawRect(
                    color = errorColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(width - stroke, height - stroke),
                    style = Stroke(width = stroke * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                return@Canvas
            }
            else -> {
                // Four-side Clockwise Continuous RGB Running Light
                // We create a sweep/perimeter gradient where phase rotates smoothly from 0 -> 1
                val colorStops = Array(RgbSpectrumList.size) { i ->
                    val frac = (i.toFloat() / (RgbSpectrumList.size - 1) + progress) % 1f
                    val color = RgbSpectrumList[i].copy(alpha = effectiveAlpha)
                    Pair(frac, color)
                }.sortedBy { it.first }

                val dynamicColors = colorStops.map { it.second }

                // Outer soft ambient glow layer (thicker line with lower opacity)
                val glowBrush = Brush.sweepGradient(
                    colors = dynamicColors,
                    center = Offset(width / 2f, height / 2f)
                )

                // Soft outer aura
                drawRect(
                    brush = glowBrush,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(width - stroke, height - stroke),
                    style = Stroke(
                        width = stroke * 2.2f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    alpha = effectiveAlpha * 0.45f
                )

                // Sharp inner core neon beam
                drawRect(
                    brush = glowBrush,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(width - stroke, height - stroke),
                    style = Stroke(
                        width = stroke,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    alpha = effectiveAlpha
                )

                // Leading bright comet highlight moving along clockwise perimeter
                val headPos = progress * perimeter
                val cometLength = perimeter * 0.18f

                // Draw bright highlight segment along perimeter
                val path = Path().apply {
                    moveTo(halfStroke, halfStroke)
                    lineTo(width - halfStroke, halfStroke) // TOP: left -> right
                    lineTo(width - halfStroke, height - halfStroke) // RIGHT: top -> bottom
                    lineTo(halfStroke, height - halfStroke) // BOTTOM: right -> left
                    lineTo(halfStroke, halfStroke) // LEFT: bottom -> top
                    close()
                }

                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, true)

                val cometPath = Path()
                val startDistance = (headPos - cometLength + perimeter) % perimeter
                val endDistance = headPos

                if (startDistance < endDistance) {
                    pathMeasure.getSegment(startDistance, endDistance, cometPath, true)
                } else {
                    pathMeasure.getSegment(startDistance, perimeter, cometPath, true)
                    pathMeasure.getSegment(0f, endDistance, cometPath, true)
                }

                drawPath(
                    path = cometPath,
                    color = Color.White.copy(alpha = effectiveAlpha * (if (state == RgbState.LISTENING) pulseScale else 0.85f)),
                    style = Stroke(
                        width = stroke * 1.4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
