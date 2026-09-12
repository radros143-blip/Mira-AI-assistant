package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.MyraAccessibilityService
import com.example.data.RgbBrightness
import com.example.data.RgbSpeed
import com.example.phone.DeviceUtils
import com.example.services.MyraNotificationListener
import com.example.ui.MainViewModel
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MyraCardBorder
import com.example.ui.theme.MyraCardSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val rgbEnabled by viewModel.rgbEnabledState.collectAsState()
    val rgbSpeed by viewModel.rgbSpeedState.collectAsState()
    val rgbBrightness by viewModel.rgbBrightnessState.collectAsState()
    val bgGlow by viewModel.bgGlowState.collectAsState()
    val visualMode24h by viewModel.visualMode24hState.collectAsState()

    val speakResponses by viewModel.speakResponsesState.collectAsState()
    val voiceSpeed by viewModel.voiceSpeedState.collectAsState()
    val voicePitch by viewModel.voicePitchState.collectAsState()
    val floatingBubbleEnabled by viewModel.floatingBubbleState.collectAsState()
    val backgroundActive by viewModel.backgroundActiveState.collectAsState()

    val isAccessibilityActive by MyraAccessibilityService.isServiceActive.collectAsState()
    val isNotificationActive by MyraNotificationListener.isListenerActive.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "सेटिंग्स (MYRA Settings)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = "अमित, अपनी व्यक्तिगत प्राथमिकताएं और प्रभाव यहाँ कस्टमाइज़ करें",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Personalized User Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MyraCardSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(NeonCyan, NeonMagenta))
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "अमित (Amit)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "व्यक्तिगत संबोधन: हमेशा \"अमित\" के रूप में",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. RGB Edge Light Settings
        SettingsGroupCard(title = "🌈 RGB Edge Light सेटिंग्स", icon = Icons.Filled.ColorLens) {
            SettingSwitchRow(
                title = "RGB Edge Light चालू रखें",
                subtitle = "स्क्रीन के चारों ओर 4-तरफ़ा क्लॉकवाइज़ रनिंग लाइट",
                checked = rgbEnabled,
                onCheckedChange = { viewModel.updateRgbEnabled(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("प्रकाश की गति (Speed):", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RgbSpeed.values().forEach { speed ->
                    val isSelected = rgbSpeed == speed
                    ChoicePill(
                        text = speed.labelHindi,
                        selected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateRgbSpeed(speed) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("प्रकाश की तीव्रता (Brightness):", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RgbBrightness.values().forEach { brightness ->
                    val isSelected = rgbBrightness == brightness
                    ChoicePill(
                        text = brightness.labelHindi,
                        selected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateRgbBrightness(brightness) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingSwitchRow(
                title = "Background Ambient Glow",
                subtitle = "अति सूक्ष्म, आँखों के लिए सुरक्षित डार्क ग्लो",
                checked = bgGlow,
                onCheckedChange = { viewModel.updateBackgroundGlow(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingSwitchRow(
                title = "24 घंटे Visual Mode",
                subtitle = "स्क्रीन चालू रहने पर निरंतर प्रकाश दिखाएं",
                checked = visualMode24h,
                onCheckedChange = { viewModel.updateVisualMode24h(it) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Voice Settings
        SettingsGroupCard(title = "🎙️ आवाज़ एवं भाषा (Voice & Speech)", icon = Icons.Filled.RecordVoiceOver) {
            SettingSwitchRow(
                title = "MYRA उत्तर बोलकर सुनाए (Voice TTS)",
                subtitle = "हिंदी आवाज़ में स्वाभाविक संवाद",
                checked = speakResponses,
                onCheckedChange = { viewModel.updateSpeakResponses(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("बोलने की गति (Speech Rate): ${String.format("%.1f", voiceSpeed)}x", fontSize = 13.sp, color = TextPrimary)
            Slider(
                value = voiceSpeed,
                onValueChange = { viewModel.updateVoiceSpeed(it) },
                valueRange = 0.7f..1.5f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("आवाज़ का सुर (Pitch): ${String.format("%.2f", voicePitch)}", fontSize = 13.sp, color = TextPrimary)
            Slider(
                value = voicePitch,
                onValueChange = { viewModel.updateVoicePitch(it) },
                valueRange = 0.8f..1.4f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = NeonMagenta,
                    activeTrackColor = NeonMagenta,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. System Permissions & Automation
        SettingsGroupCard(title = "🛡️ सिस्टम अनुमतियाँ एवं बैकग्राउंड सेवा", icon = Icons.Filled.Security) {
            val isOverlayGranted = DeviceUtils.isOverlayPermissionGranted(context)

            SettingSwitchRow(
                title = "निरंतर बैकग्राउंड सेवा (Background Service)",
                subtitle = "ऐप बंद होने पर भी बैकग्राउंड में सक्रिय रहे",
                checked = backgroundActive,
                onCheckedChange = { viewModel.updateBackgroundActiveEnabled(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                title = "फ्लोटिंग माइक बबल (Floating Assistant Bubble)",
                subtitle = "अन्य ऐप्स के ऊपर फ्लोटिंग माइक आइकन दिखाएं",
                checked = floatingBubbleEnabled,
                onCheckedChange = { viewModel.updateFloatingBubbleEnabled(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PermissionStatusRow(
                title = "सिस्टम स्क्रीन ओवरले (Display over other apps)",
                active = isOverlayGranted,
                onOpen = { DeviceUtils.openOverlayPermissionSettings(context) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PermissionStatusRow(
                title = "Accessibility Service",
                active = isAccessibilityActive,
                onOpen = { DeviceUtils.openAccessibilitySettings(context) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PermissionStatusRow(
                title = "Notification Listener",
                active = isNotificationActive,
                onOpen = { DeviceUtils.openNotificationListenerSettings(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.restartBackgroundService() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("बैकग्राउंड सेवा रीस्टार्ट करें (Restart Service)", color = NeonCyan, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "गोपनीयता नीति (Privacy): MYRA हमेशा अमित के फ़ोन पर बैकग्राउंड में सुरक्षित चलती है। कोई भी डेटा बाहरी सर्वर पर बिना अनुमति नहीं भेजा जाता।",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. About
        SettingsGroupCard(title = "ℹ️ About MYRA Assistant", icon = Icons.Filled.Info) {
            Text(
                text = "MYRA AI Assistant — Amit Edition\nसंस्करण: 1.0.0 (Native Kotlin / Jetpack Compose)\nडिज़ाइन: Futuristic RGB + Native Android Phone Control",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MyraCardSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(MyraCardBorder, Color.White.copy(alpha = 0.08f)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun ChoicePill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (selected) NeonCyan else MyraCardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) NeonCyan else TextSecondary
        )
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    active: Boolean,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(
                text = if (active) "सक्रिय (Active)" else "निष्क्रिय (Disabled)",
                fontSize = 11.sp,
                color = if (active) SuccessGreen else ErrorRed
            )
        }
        OutlinedButton(
            onClick = onOpen,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("बदलें", fontSize = 11.sp)
        }
    }
}
