package com.example.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.MyraPreferences
import com.example.data.RgbBrightness
import com.example.data.RgbSpeed
import com.example.ui.components.RgbState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyraForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayView: RgbEdgeOverlayView? = null
    private var bubbleView: FloatingBubbleView? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var prefs: MyraPreferences

    companion object {
        const val CHANNEL_ID = "myra_ai_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.services.ACTION_START"
        const val ACTION_STOP = "com.example.services.ACTION_STOP"
        const val ACTION_TOGGLE_RGB = "com.example.services.ACTION_TOGGLE_RGB"
        const val ACTION_TOGGLE_BUBBLE = "com.example.services.ACTION_TOGGLE_BUBBLE"
        const val ACTION_TRIGGER_VOICE = "com.example.services.ACTION_TRIGGER_VOICE"
        const val ACTION_UPDATE_CONFIG = "com.example.services.ACTION_UPDATE_CONFIG"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _isOverlayActive = MutableStateFlow(false)
        val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

        private var instance: MyraForegroundService? = null

        fun startService(context: Context) {
            val intent = Intent(context, MyraForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MyraForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateOverlayState(state: RgbState) {
            instance?.setOverlayState(state)
        }

        fun updateConfig(context: Context) {
            val intent = Intent(context, MyraForegroundService::class.java).apply {
                action = ACTION_UPDATE_CONFIG
            }
            context.startService(intent)
        }
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = MyraPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        createNotificationChannel()

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Myra::BackgroundWakeLock")
            wakeLock?.acquire(10 * 60 * 1000L /* 10 minutes */)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_RGB -> {
                val newRgb = !prefs.rgbEnabled
                prefs.rgbEnabled = newRgb
                updateOverlay()
                updateNotification()
            }
            ACTION_TOGGLE_BUBBLE -> {
                val newBubble = !prefs.floatingBubbleEnabled
                prefs.floatingBubbleEnabled = newBubble
                updateOverlay()
                updateNotification()
            }
            ACTION_TRIGGER_VOICE -> {
                // Launch MainActivity with trigger voice flag
                val openIntent = Intent(this, MainActivity::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("TRIGGER_VOICE", true)
                }
                startActivity(openIntent)
            }
            ACTION_UPDATE_CONFIG -> {
                updateOverlay()
            }
            else -> {
                // ACTION_START or Default
                startAsForeground()
                updateOverlay()
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        _isServiceRunning.value = true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MYRA AI Assistant Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "MYRA निरंतर बैकग्राउंड असिस्टेंट, फ्लोटिंग माइक और RGB Edge Light चलाता है"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val voiceIntent = Intent(this, MyraForegroundService::class.java).apply {
            action = ACTION_TRIGGER_VOICE
        }
        val voicePendingIntent = PendingIntent.getService(
            this, 1, voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rgbIntent = Intent(this, MyraForegroundService::class.java).apply {
            action = ACTION_TOGGLE_RGB
        }
        val rgbPendingIntent = PendingIntent.getService(
            this, 2, rgbIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MyraForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rgbStatusText = if (prefs.rgbEnabled) "RGB चालू" else "RGB बंद"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("नमस्ते अमित! MYRA Assistant बैकग्राउंड में सक्रिय है")
            .setContentText("बैकग्राउंड सेवा चालू • $rgbStatusText • कहीं से भी कमांड देने के लिए तैयार")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.mipmap.ic_launcher, "🎙️ बोलें (Speak)", voicePendingIntent)
            .addAction(R.mipmap.ic_launcher, "🌈 $rgbStatusText", rgbPendingIntent)
            .addAction(R.mipmap.ic_launcher, "🛑 बंद करें", stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun updateOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            removeOverlay()
            removeBubble()
            _isOverlayActive.value = false
            return
        }

        // 1. RGB Edge Light Overlay
        if (prefs.rgbEnabled) {
            if (overlayView == null) {
                val view = RgbEdgeOverlayView(this)
                view.setConfig(
                    enabled = prefs.rgbEnabled,
                    newSpeed = prefs.rgbSpeed,
                    newBrightness = prefs.rgbBrightness,
                    newState = RgbState.NORMAL
                )

                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                }

                try {
                    windowManager?.addView(view, params)
                    overlayView = view
                    _isOverlayActive.value = true
                } catch (e: Exception) {
                    _isOverlayActive.value = false
                }
            } else {
                overlayView?.setConfig(
                    enabled = prefs.rgbEnabled,
                    newSpeed = prefs.rgbSpeed,
                    newBrightness = prefs.rgbBrightness,
                    newState = RgbState.NORMAL
                )
                _isOverlayActive.value = true
            }
        } else {
            removeOverlay()
        }

        // 2. Floating Quick Assistant Bubble
        if (prefs.floatingBubbleEnabled) {
            if (bubbleView == null && windowManager != null) {
                val bubbleSize = (64 * resources.displayMetrics.density).toInt()
                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val bubbleParams = WindowManager.LayoutParams(
                    bubbleSize,
                    bubbleSize,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 10
                    y = (resources.displayMetrics.heightPixels * 0.45f).toInt()
                }

                val bView = FloatingBubbleView(this, windowManager!!, bubbleParams) {
                    // On Tap Bubble: Launch Voice Assistant
                    val triggerIntent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("TRIGGER_VOICE", true)
                    }
                    startActivity(triggerIntent)
                }

                try {
                    windowManager?.addView(bView, bubbleParams)
                    bubbleView = bView
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } else {
            removeBubble()
        }
    }

    fun setOverlayState(state: RgbState) {
        overlayView?.setState(state)
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // Ignore
            }
            overlayView = null
        }
    }

    private fun removeBubble() {
        bubbleView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // Ignore
            }
            bubbleView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        removeBubble()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        _isServiceRunning.value = false
        _isOverlayActive.value = false
        serviceScope.cancel()
        instance = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
