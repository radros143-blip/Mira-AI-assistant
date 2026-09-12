package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.accessibility.MyraAccessibilityService
import com.example.phone.DeviceUtils
import com.example.services.MyraForegroundService
import com.example.services.MyraNotificationListener
import com.example.ui.MainViewModel
import com.example.ui.components.BackgroundRgbGlow
import com.example.ui.components.CallConfirmationDialog
import com.example.ui.components.MyraBottomNavigation
import com.example.ui.components.MyraScreen
import com.example.ui.components.RgbEdgeLight
import com.example.ui.screens.ActionsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StartupSplashScreen
import com.example.ui.theme.MyraAssistantTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            MyraForegroundService.startService(this)
        } catch (e: Exception) {
            // Ignore
        }

        handleVoiceTrigger(intent)

        setContent {
            MyraAssistantTheme {
                MyraApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            MyraForegroundService.startService(this)
            MyraForegroundService.updateConfig(this)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleVoiceTrigger(intent)
    }

    private fun handleVoiceTrigger(intent: Intent?) {
        if (intent?.getBooleanExtra("TRIGGER_VOICE", false) == true) {
            viewModel.setScreen(MyraScreen.HOME)
            viewModel.toggleVoiceListening()
        }
    }
}

@Composable
fun MyraApp(viewModel: MainViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val rgbState by viewModel.rgbState.collectAsState()
    val rgbEnabled by viewModel.rgbEnabledState.collectAsState()
    val rgbSpeed by viewModel.rgbSpeedState.collectAsState()
    val rgbBrightness by viewModel.rgbBrightnessState.collectAsState()
    val bgGlowEnabled by viewModel.bgGlowState.collectAsState()
    val pendingCallRequest by viewModel.pendingCallRequest.collectAsState()

    val isAccessibilityActive by MyraAccessibilityService.isServiceActive.collectAsState()
    val isNotificationActive by MyraNotificationListener.isListenerActive.collectAsState()

    // Request permissions dynamically
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    if (showSplash) {
        StartupSplashScreen(
            onFinished = { showSplash = false },
            onPlayGreeting = { viewModel.playStartupGreeting() }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient RGB Background Glow
            BackgroundRgbGlow(enabled = bgGlowEnabled)

            // Main Screen Scaffold
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                bottomBar = {
                    MyraBottomNavigation(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen ->
                            viewModel.setScreen(screen)
                        }
                    )
                }
            ) { _ ->
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        MyraScreen.HOME -> HomeScreen(
                            viewModel = viewModel,
                            isAccessibilityEnabled = isAccessibilityActive,
                            isNotificationListenerEnabled = isNotificationActive
                        )
                        MyraScreen.CHAT -> ChatScreen(
                            viewModel = viewModel
                        )
                        MyraScreen.ACTIONS -> ActionsScreen(
                            viewModel = viewModel
                        )
                        MyraScreen.SETTINGS -> SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Continuous 4-Sided Clockwise RGB Edge Light Overlay
            RgbEdgeLight(
                enabled = rgbEnabled,
                speed = rgbSpeed,
                brightness = rgbBrightness,
                state = rgbState
            )

            // Call Confirmation Dialog
            pendingCallRequest?.let { req ->
                CallConfirmationDialog(
                    callRequest = req,
                    onConfirm = { viewModel.confirmCall() },
                    onDismiss = { viewModel.cancelCall() }
                )
            }
        }
    }
}
