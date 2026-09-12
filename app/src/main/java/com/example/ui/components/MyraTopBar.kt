package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MyraTopBar(
    userName: String = "अमित",
    isAccessibilityEnabled: Boolean,
    isNotificationListenerEnabled: Boolean,
    isRgbActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("myra_top_bar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status badge bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MYRA AI ASSISTANT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                letterSpacing = 4.sp
            )

            // Status Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    active = isRgbActive,
                    label = "RGB",
                    activeColor = NeonMagenta
                )
                StatusPill(
                    active = isAccessibilityEnabled,
                    label = "ACC",
                    activeColor = NeonCyan
                )
                StatusPill(
                    active = isNotificationListenerEnabled,
                    label = "NOTIF",
                    activeColor = SuccessGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline: "नमस्ते अमित!"
        val annotatedGreeting = buildAnnotatedString {
            append("नमस्ते ")
            withStyle(style = SpanStyle(color = NeonCyan, fontWeight = FontWeight.Bold)) {
                append(userName)
            }
            append("!")
        }

        Text(
            text = annotatedGreeting,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "मैं आपकी मदद के लिए तैयार हूँ।",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatusPill(
    active: Boolean,
    label: String,
    activeColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) activeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (active) activeColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (active) activeColor else TextMuted)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) TextPrimary else TextMuted
            )
        }
    }
}

