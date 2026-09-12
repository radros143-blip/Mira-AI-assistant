package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RgbBrightness
import com.example.data.RgbSpeed
import com.example.ui.components.BackgroundRgbGlow
import com.example.ui.components.RgbEdgeLight
import com.example.ui.components.RgbState
import com.example.ui.theme.MyraBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun StartupSplashScreen(
    onFinished: () -> Unit,
    onPlayGreeting: () -> Unit
) {
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Step 1: Show MYRA & AI ASSISTANT
        delay(400)
        step = 1

        // Step 2: Show "नमस्ते अमित ❤️"
        delay(900)
        step = 2

        // Step 3: Show "मैं MYRA हूँ।"
        delay(900)
        step = 3
        onPlayGreeting()

        // Step 4: Show "बताइए अमित, आज मैं आपकी क्या मदद करूँ?"
        delay(1100)
        step = 4

        // Complete splash
        delay(1600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyraBlack)
            .clickable { onFinished() }
            .testTag("startup_splash_screen")
    ) {
        // Subtle ambient glow
        BackgroundRgbGlow(enabled = true)

        // Running RGB Edge light active from startup
        RgbEdgeLight(
            enabled = true,
            speed = RgbSpeed.NORMAL,
            brightness = RgbBrightness.HIGH,
            state = RgbState.NORMAL
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing AI Core Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonCyan.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Title
            AnimatedVisibility(
                visible = step >= 1,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MYRA",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp,
                            brush = Brush.linearGradient(
                                listOf(NeonCyan, Color.White, NeonMagenta)
                            )
                        )
                    )
                    Text(
                        text = "AI ASSISTANT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Step 2: Personalized greeting
            AnimatedVisibility(
                visible = step >= 2,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 30 }
            ) {
                Text(
                    text = "नमस्ते अमित ❤️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step 3: Identity
            AnimatedVisibility(
                visible = step >= 3,
                enter = fadeIn(tween(600))
            ) {
                Text(
                    text = "मैं MYRA हूँ।",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeonMagenta,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 4: Ready message
            AnimatedVisibility(
                visible = step >= 4,
                enter = fadeIn(tween(600))
            ) {
                Text(
                    text = "बताइए अमित, आज मैं आपकी क्या मदद करूँ?",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Tap anywhere to continue",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
