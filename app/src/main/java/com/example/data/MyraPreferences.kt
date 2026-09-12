package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class RgbSpeed(val durationMs: Int, val labelHindi: String) {
    SLOW(8000, "धीमी (Slow)"),
    NORMAL(4500, "सामान्य (Normal)"),
    FAST(2000, "तेज़ (Fast)")
}

enum class RgbBrightness(val alphaFactor: Float, val strokeWidthDp: Float, val labelHindi: String) {
    LOW(0.5f, 3f, "कम (Low)"),
    MEDIUM(0.85f, 4.5f, "मध्यम (Medium)"),
    HIGH(1.0f, 6.5f, "अधिक (High)")
}

class MyraPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("myra_ai_preferences", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "अमित") ?: "अमित"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var rgbEnabled: Boolean
        get() = prefs.getBoolean(KEY_RGB_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_RGB_ENABLED, value).apply()

    var rgbSpeed: RgbSpeed
        get() = try {
            RgbSpeed.valueOf(prefs.getString(KEY_RGB_SPEED, RgbSpeed.NORMAL.name) ?: RgbSpeed.NORMAL.name)
        } catch (e: Exception) {
            RgbSpeed.NORMAL
        }
        set(value) = prefs.edit().putString(KEY_RGB_SPEED, value.name).apply()

    var rgbBrightness: RgbBrightness
        get() = try {
            RgbBrightness.valueOf(prefs.getString(KEY_RGB_BRIGHTNESS, RgbBrightness.MEDIUM.name) ?: RgbBrightness.MEDIUM.name)
        } catch (e: Exception) {
            RgbBrightness.MEDIUM
        }
        set(value) = prefs.edit().putString(KEY_RGB_BRIGHTNESS, value.name).apply()

    var backgroundGlowEnabled: Boolean
        get() = prefs.getBoolean(KEY_BG_GLOW, true)
        set(value) = prefs.edit().putBoolean(KEY_BG_GLOW, value).apply()

    var visualMode24h: Boolean
        get() = prefs.getBoolean(KEY_VISUAL_24H, true)
        set(value) = prefs.edit().putBoolean(KEY_VISUAL_24H, value).apply()

    var speakResponses: Boolean
        get() = prefs.getBoolean(KEY_SPEAK_RESPONSES, true)
        set(value) = prefs.edit().putBoolean(KEY_SPEAK_RESPONSES, value).apply()

    var voiceSpeed: Float
        get() = prefs.getFloat(KEY_VOICE_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_SPEED, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 1.05f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var voiceLanguage: String
        get() = prefs.getString(KEY_VOICE_LANG, "hi-IN") ?: "hi-IN"
        set(value) = prefs.edit().putString(KEY_VOICE_LANG, value).apply()

    var floatingBubbleEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLOATING_BUBBLE, true)
        set(value) = prefs.edit().putBoolean(KEY_FLOATING_BUBBLE, value).apply()

    var backgroundActiveEnabled: Boolean
        get() = prefs.getBoolean(KEY_BACKGROUND_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_BACKGROUND_ACTIVE, value).apply()

    var startupGreetingPlayed: Boolean
        get() = prefs.getBoolean(KEY_STARTUP_GREETING, false)
        set(value) = prefs.edit().putBoolean(KEY_STARTUP_GREETING, value).apply()

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_RGB_ENABLED = "rgb_enabled"
        private const val KEY_RGB_SPEED = "rgb_speed"
        private const val KEY_RGB_BRIGHTNESS = "rgb_brightness"
        private const val KEY_BG_GLOW = "background_glow"
        private const val KEY_VISUAL_24H = "visual_mode_24h"
        private const val KEY_SPEAK_RESPONSES = "speak_responses"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_VOICE_LANG = "voice_lang"
        private const val KEY_FLOATING_BUBBLE = "floating_bubble"
        private const val KEY_BACKGROUND_ACTIVE = "background_active"
        private const val KEY_STARTUP_GREETING = "startup_greeting_played"
    }
}
