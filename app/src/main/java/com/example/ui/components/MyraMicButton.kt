package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.voice.SpeechStatus

@Composable
fun MyraMicButton(
    status: SpeechStatus,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 130.dp
) {
    val isListening = status == SpeechStatus.LISTENING
    val isProcessing = status == SpeechStatus.PROCESSING

    val transition = rememberInfiniteTransition(label = "MicCoreAnimation")

    // Pulsing scale for listening / active voice state
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening || isSpeaking) 1.10f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 700 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Concentric halo scale & alpha
    val haloScale by transition.animateFloat(
        initialValue = 1.25f,
        targetValue = if (isListening) 1.55f else 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 900 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    val haloAlpha by transition.animateFloat(
        initialValue = if (isListening) 0.35f else 0.18f,
        targetValue = if (isListening) 0.55f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 900 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloAlpha"
    )

    Box(
        modifier = modifier
            .size(sizeDp + 70.dp)
            .testTag("myra_mic_core_container"),
        contentAlignment = Alignment.Center
    ) {
        // Multi-layered Concentric Rings & Glow Aura
        Canvas(modifier = Modifier.size(sizeDp + 70.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = sizeDp.toPx() / 2f

            // 1. Soft Ambient Blur Aura (outermost)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = haloAlpha),
                        NeonPurple.copy(alpha = haloAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * haloScale
                ),
                radius = baseRadius * haloScale,
                center = center
            )

            // 2. Outermost fine concentric ring (scale 1.50)
            drawCircle(
                color = if (isListening) NeonCyan.copy(alpha = 0.45f) else NeonCyan.copy(alpha = 0.25f),
                radius = baseRadius * 1.50f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 3. Middle concentric ring (scale 1.20)
            drawCircle(
                color = Color.White.copy(alpha = if (isListening) 0.25f else 0.12f),
                radius = baseRadius * 1.20f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Central Zinc-900 Core Button
        Box(
            modifier = Modifier
                .size(sizeDp)
                .scale(pulseScale)
                .shadow(
                    elevation = if (isListening) 24.dp else 14.dp,
                    shape = CircleShape,
                    ambientColor = if (isListening) NeonCyan else Color.Black,
                    spotColor = if (isListening) NeonCyan else NeonPurple
                )
                .clip(CircleShape)
                .background(Color(0xFF18181B)) // zinc-900
                .border(
                    width = 2.dp,
                    color = if (isListening) NeonCyan else Color.White.copy(alpha = 0.20f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = NeonCyan)
                ) {
                    onClick()
                }
                .testTag("mic_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            // Gradient shimmer overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = if (isListening) 0.25f else 0.12f),
                                NeonPurple.copy(alpha = if (isListening) 0.25f else 0.12f)
                            )
                        )
                    )
            )

            val iconTint = when {
                isListening -> NeonCyan
                isProcessing -> NeonMagenta
                isSpeaking -> Color(0xFF22C55E)
                else -> Color.White
            }

            Icon(
                imageVector = when {
                    isListening -> Icons.Filled.Stop
                    else -> Icons.Filled.Mic
                },
                contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                tint = iconTint,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

