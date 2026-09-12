package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MyraScreen(val labelHindi: String, val testTag: String) {
    HOME("Home", "nav_home"),
    CHAT("Chat", "nav_chat"),
    ACTIONS("Actions", "nav_actions"),
    SETTINGS("Settings", "nav_settings")
}

@Composable
fun MyraBottomNavigation(
    currentScreen: MyraScreen,
    onScreenSelected: (MyraScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("myra_bottom_navigation")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                screen = MyraScreen.HOME,
                selected = currentScreen == MyraScreen.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onScreenSelected(MyraScreen.HOME) }
            )
            NavItem(
                screen = MyraScreen.CHAT,
                selected = currentScreen == MyraScreen.CHAT,
                selectedIcon = Icons.Filled.Chat,
                unselectedIcon = Icons.Outlined.Chat,
                onClick = { onScreenSelected(MyraScreen.CHAT) }
            )
            NavItem(
                screen = MyraScreen.ACTIONS,
                selected = currentScreen == MyraScreen.ACTIONS,
                selectedIcon = Icons.Filled.AutoAwesome,
                unselectedIcon = Icons.Outlined.SmartToy,
                onClick = { onScreenSelected(MyraScreen.ACTIONS) }
            )
            NavItem(
                screen = MyraScreen.SETTINGS,
                selected = currentScreen == MyraScreen.SETTINGS,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = { onScreenSelected(MyraScreen.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    screen: MyraScreen,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, color = NeonCyan)
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag(screen.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) NeonCyan.copy(alpha = 0.20f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = screen.labelHindi,
                tint = if (selected) NeonCyan else Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = screen.labelHindi,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) NeonCyan else Color.White.copy(alpha = 0.45f),
            letterSpacing = (-0.2).sp
        )
    }
}

