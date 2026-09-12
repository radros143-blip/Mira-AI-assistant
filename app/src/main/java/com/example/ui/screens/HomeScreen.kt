package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commands.StepStatus
import com.example.ui.MainViewModel
import com.example.ui.components.MyraMicButton
import com.example.ui.components.MyraTopBar
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MyraCardBorder
import com.example.ui.theme.MyraCardSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.SpeechStatus

import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.platform.LocalContext
import com.example.phone.DeviceUtils

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    isAccessibilityEnabled: Boolean,
    isNotificationListenerEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOverlayGranted = DeviceUtils.isOverlayPermissionGranted(context)

    val speechStatus by viewModel.speechRecognizer.status.collectAsState()
    val recognizedText by viewModel.speechRecognizer.recognizedText.collectAsState()
    val partialText by viewModel.speechRecognizer.partialText.collectAsState()
    val lastResponse by viewModel.lastAssistantResponse.collectAsState()
    val isSpeaking by viewModel.textToSpeech.isSpeaking.collectAsState()
    val activeMultiStepPlan by viewModel.activeMultiStepPlan.collectAsState()
    val rgbEnabled by viewModel.rgbEnabledState.collectAsState()
    val floatingBubbleEnabled by viewModel.floatingBubbleState.collectAsState()
    val backgroundActive by viewModel.backgroundActiveState.collectAsState()

    val isListening = speechStatus == SpeechStatus.LISTENING
    val isProcessing = speechStatus == SpeechStatus.PROCESSING

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        MyraTopBar(
            userName = viewModel.prefs.userName,
            isAccessibilityEnabled = isAccessibilityEnabled,
            isNotificationListenerEnabled = isNotificationListenerEnabled,
            isRgbActive = rgbEnabled
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🌟 Background Mode Alert / Status Banner
        if (!isOverlayGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("background_permission_alert_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1B0E)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444)))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "बैकग्राउंड मोड सक्रिय करें (Action Required)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ऐप बंद होने के बाद भी स्क्रीन के चारों तरफ RGB Light चलाने और फ्लोटिंग माइक से कहीं से भी बात करने के लिए 'Display over other apps' अनुमति आवश्यक है।",
                        fontSize = 12.sp,
                        color = Color(0xFFE5E7EB),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            DeviceUtils.openOverlayPermissionSettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_overlay_permission_button")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "अभी अनुमति दें (Enable Background Mode)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            // Background Mode Active Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .testTag("background_active_status_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MyraCardSurface.copy(alpha = 0.9f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.6f), NeonGreen.copy(alpha = 0.6f)))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🟢 बैकग्राउंड सर्विस सक्रिय है",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }

                        Text(
                            text = "RGB + फ्लोटिंग माइक",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Floating Bubble Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.updateFloatingBubbleEnabled(!floatingBubbleEnabled)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = null,
                                tint = if (floatingBubbleEnabled) NeonCyan else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "फ्लोटिंग माइक",
                                fontSize = 12.sp,
                                color = if (floatingBubbleEnabled) TextPrimary else TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = floatingBubbleEnabled,
                                onCheckedChange = { viewModel.updateFloatingBubbleEnabled(it) },
                                modifier = Modifier.size(24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeonCyan,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }

                        // RGB Edge Light Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.updateRgbEnabled(!rgbEnabled)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Radio,
                                contentDescription = null,
                                tint = if (rgbEnabled) NeonGreen else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RGB लाइट",
                                fontSize = 12.sp,
                                color = if (rgbEnabled) TextPrimary else TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = rgbEnabled,
                                onCheckedChange = { viewModel.updateRgbEnabled(it) },
                                modifier = Modifier.size(24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeonGreen,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Assistant Status / Response Bubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MyraCardSurface.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
                .testTag("assistant_status_card")
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = if (isSpeaking) NeonGreen else NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isListening -> "सुन रही हूँ अमित..."
                                isProcessing -> "समझ रही हूँ..."
                                isSpeaking -> "MYRA बोल रही है..."
                                else -> "MYRA Assistant"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isListening) NeonCyan else TextSecondary
                        )
                    }

                    if (isSpeaking || isListening) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isListening) NeonCyan else NeonGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display live recognized text or latest response
                val displayStatusText = when {
                    isListening && partialText.isNotBlank() -> "\"$partialText\""
                    isListening -> "अमित, बताइए मैं आपकी क्या मदद करूँ?"
                    isProcessing -> "कमांड प्रोसेस की जा रही है..."
                    recognizedText.isNotBlank() -> "\"$recognizedText\""
                    else -> lastResponse
                }

                Text(
                    text = displayStatusText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Center Futuristic Hologram Microphone Core
        MyraMicButton(
            status = speechStatus,
            isSpeaking = isSpeaking,
            onClick = {
                viewModel.toggleVoiceListening()
            },
            sizeDp = 128.dp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // State indicator label (font-mono tracking-widest uppercase)
        val stateLabel = when {
            isListening -> "LISTENING"
            isProcessing -> "PROCESSING"
            isSpeaking -> "SPEAKING"
            else -> "TAP TO TALK"
        }

        Text(
            text = stateLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isListening) NeonCyan else TextSecondary,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Prompt / query suggestion or active transcription
        val promptQuote = when {
            isListening && partialText.isNotBlank() -> "\"$partialText\""
            isListening -> "\"MYRA YouTube खोलो\""
            isProcessing -> "\"आदेश प्रोसेस हो रहा है...\""
            recognizedText.isNotBlank() -> "\"$recognizedText\""
            else -> "\"MYRA YouTube खोलकर अमित कुमार सर्च करो\""
        }

        Text(
            text = promptQuote,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))


        // Multi-step Action Planner Card (if active)
        AnimatedVisibility(
            visible = activeMultiStepPlan != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            activeMultiStepPlan?.let { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("multi_step_progress_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MyraCardSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonCyan, NeonMagenta))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ AI Action Planner: ${plan.planSummary}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        plan.steps.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (step.status) {
                                    StepStatus.COMPLETED -> Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    StepStatus.RUNNING -> CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = NeonCyan
                                    )
                                    StepStatus.FAILED -> Icon(
                                        Icons.Filled.Error,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    StepStatus.PENDING -> Icon(
                                        Icons.Filled.HourglassTop,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.titleHindi,
                                    fontSize = 13.sp,
                                    color = if (step.status == StepStatus.RUNNING) NeonCyan else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (step.statusMessage.isNotBlank()) {
                                    Text(
                                        text = step.statusMessage,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Suggestions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "त्वरित कमांड्स (Quick Commands)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    icon = Icons.Filled.PlayArrow,
                    label = "YouTube खोलो",
                    iconColor = Color(0xFFFF0033),
                    onClick = { viewModel.processCommand("YouTube खोलो") }
                )

                QuickActionChip(
                    icon = Icons.Filled.Search,
                    label = "YouTube search Amit Kumar",
                    iconColor = NeonCyan,
                    onClick = { viewModel.processCommand("YouTube खोलकर Amit Kumar search करो") }
                )
                QuickActionChip(
                    icon = Icons.Filled.Phone,
                    label = "मम्मी को call करो",
                    iconColor = NeonGreen,
                    onClick = { viewModel.processCommand("मम्मी को call करो") }
                )
                QuickActionChip(
                    icon = Icons.Filled.Notifications,
                    label = "Notifications पढ़ो",
                    iconColor = NeonMagenta,
                    onClick = { viewModel.processCommand("MYRA मेरी notifications पढ़ो") }
                )
                QuickActionChip(
                    icon = Icons.Filled.FlashlightOn,
                    label = "Flashlight जलाओ",
                    iconColor = Color(0xFFFFD700),
                    onClick = { viewModel.processCommand("Flashlight जलाओ") }
                )
                QuickActionChip(
                    icon = Icons.Filled.VolumeUp,
                    label = "Volume बढ़ाओ",
                    iconColor = NeonPurple,
                    onClick = { viewModel.processCommand("Volume बढ़ाओ") }
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom navigation
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MyraCardSurface)
            .border(1.dp, MyraCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("quick_chip_${label.replace(" ", "_").lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
