package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var preferredLanguage: String = "hi-IN"
    private var speechPitch: Float = 1.05f
    private var speechRate: Float = 1.0f

    var onSpeechCompletedListener: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureLanguage(preferredLanguage)
            tts?.setPitch(speechPitch)
            tts?.setSpeechRate(speechRate)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechCompletedListener?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            _isInitialized.value = true
        } else {
            _isInitialized.value = false
        }
    }

    fun configureLanguage(langCode: String) {
        preferredLanguage = langCode
        val t = tts ?: return

        val targetLocale = if (langCode.startsWith("hi")) {
            Locale("hi", "IN")
        } else {
            Locale("en", "IN")
        }

        val result = t.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to Hindi simple or English
            val fallbackResult = t.setLanguage(Locale("hi"))
            if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                t.setLanguage(Locale.ENGLISH)
            }
        }
    }

    fun setSpeechParameters(pitch: Float, rate: Float) {
        speechPitch = pitch
        speechRate = rate
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String, flushQueue: Boolean = true) {
        if (text.isBlank() || tts == null || !_isInitialized.value) return

        val queueMode = if (flushQueue) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle()
        val utteranceId = "myra_utterance_${System.currentTimeMillis()}"

        try {
            tts?.speak(text, queueMode, params, utteranceId)
        } catch (e: Exception) {
            _isSpeaking.value = false
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
