package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.MyraAccessibilityService
import com.example.phone.DeviceUtils
import com.example.services.MyraNotificationListener
import com.example.ui.MainViewModel
import com.example.ui.theme.MyraCardBorder
import com.example.ui.theme.MyraCardSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTorchActive by viewModel.isTorchActiveState.collectAsState()
    val notificationsList by MyraNotificationListener.notificationsList.collectAsState()
    val isAccessibilityActive by MyraAccessibilityService.isServiceActive.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("actions_screen")
    ) {
        Text(
            text = "फ़ोन कंट्रोल सेंटर (Phone Controls)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = "अमित, यहाँ से सीधे अपने फ़ोन के टूल्स और ऑटोमेशन नियंत्रित करें",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Device Hardware Controls Card
        SectionCard(title = "⚡ डिवाइस टूल्स (Device Hardware)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = if (isTorchActive) "Torch बंद" else "Torch चालू",
                    subtitle = if (isTorchActive) "ON" else "OFF",
                    icon = if (isTorchActive) Icons.Filled.FlashlightOff else Icons.Filled.FlashlightOn,
                    iconColor = if (isTorchActive) Color(0xFFFFD700) else TextMuted,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.toggleTorchDirectly() }
                )

                val batteryLevel = DeviceUtils.getBatteryLevel(context)
                ActionTile(
                    title = "Battery",
                    subtitle = if (batteryLevel >= 0) "$batteryLevel%" else "जाँचें",
                    icon = Icons.Filled.BatteryChargingFull,
                    iconColor = NeonGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("बैटरी स्थिति बताओ") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Volume Up",
                    subtitle = "+ बढ़ाएं",
                    icon = Icons.Filled.VolumeUp,
                    iconColor = NeonCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Volume बढ़ाओ") }
                )

                ActionTile(
                    title = "Volume Down",
                    subtitle = "- घटाएं",
                    icon = Icons.Filled.VolumeDown,
                    iconColor = NeonPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Volume कम करो") }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Accessibility Navigation Controller
        SectionCard(title = "🤖 स्क्रीन ऑटोमेशन (Accessibility Actions)") {
            if (!isAccessibilityActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonMagenta.copy(alpha = 0.12f))
                        .border(1.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Accessibility Service सक्रिय नहीं है",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "स्क्रीन नेविगेशन और ऑटोमेशन के लिए सेटिंग्स में MYRA चालू करें।",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { DeviceUtils.openAccessibilitySettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Settings खोलें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallNavButton(
                    label = "Back",
                    icon = Icons.Filled.ArrowBack,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Back जाओ") }
                )
                SmallNavButton(
                    label = "Home",
                    icon = Icons.Filled.Home,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Home जाओ") }
                )
                SmallNavButton(
                    label = "Scroll ↑",
                    icon = Icons.Filled.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("ऊपर scroll करो") }
                )
                SmallNavButton(
                    label = "Scroll ↓",
                    icon = Icons.Filled.ArrowDownward,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("नीचे scroll करो") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.processCommand("स्क्रीन पढ़ो") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("वर्तमान स्क्रीन पढ़कर सुनाओ (Read Screen)", fontSize = 13.sp, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Multi-Step Automation Runner
        SectionCard(title = "⚙️ AI Multi-Step Planner") {
            Text(
                text = "MYRA एक से अधिक चरणों वाले जटिल कार्यों को आसानी से निष्पादित करती है:",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            val sampleTasks = listOf(
                "YouTube खोलकर Amit Kumar search करो",
                "Chrome खोलकर ताज़ा समाचार सर्च करो",
                "Play Store खोलकर WhatsApp search करो"
            )

            sampleTasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .clickable { viewModel.processCommand(task) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "▶ $task",
                        fontSize = 13.sp,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Run",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Notifications Reader Card
        SectionCard(title = "🔔 लाइव नोटिफिकेशन्स (Live Notifications)") {
            if (notificationsList.isEmpty()) {
                Text(
                    text = "अभी कोई नई notification नहीं है या Notification Access सक्षम नहीं है।",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { DeviceUtils.openNotificationListenerSettings(context) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Notification Access सेटिंग खोलें", fontSize = 12.sp)
                }
            } else {
                notificationsList.take(5).forEach { notif ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${notif.appName}: ${notif.title}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        if (notif.text.isNotBlank()) {
                            Text(
                                text = notif.text,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.processCommand("MYRA मेरी notifications पढ़ो") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("सभी Notifications पढ़कर सुनाओ", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
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
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, MyraCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SmallNavButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, MyraCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
