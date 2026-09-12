package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechStatus {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}

class SpeechRecognizerManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _status = MutableStateFlow(SpeechStatus.IDLE)
    val status: StateFlow<SpeechStatus> = _status.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onResultListener: ((String) -> Unit)? = null

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Speech recognition initialization failed: ${e.message}"
            }
        } else {
            _errorMessage.value = "Speech recognition is not available on this device"
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _status.value = SpeechStatus.LISTENING
                _errorMessage.value = null
            }

            override fun onBeginningOfSpeech() {
                _status.value = SpeechStatus.LISTENING
            }

            override fun onRmsChanged(rmsdB: Float) {
                // RMS audio amplitude feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _status.value = SpeechStatus.PROCESSING
            }

            override fun onError(error: Int) {
                _status.value = SpeechStatus.ERROR
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "कोई आवाज़ सुनाई नहीं दी (No match)"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "समय समाप्त हो गया (Speech timeout)"
                    SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्डिंग में त्रुटि (Audio error)"
                    SpeechRecognizer.ERROR_CLIENT -> "रिकॉग्निशन रोक दी गई (Client stopped)"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफ़ोन अनुमति आवश्यक है (Permission required)"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "इंटरनेट कनेक्शन की जाँच करें (Network error)"
                    else -> "पहचान में समस्या आई (Code $error)"
                }
                _errorMessage.value = message
            }

            override fun onResults(results: Bundle?) {
                _status.value = SpeechStatus.IDLE
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    _recognizedText.value = text
                    _partialText.value = ""
                    onResultListener?.invoke(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    _partialText.value = text
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening(languageCode: String = "hi-IN") {
        if (speechRecognizer == null) {
            initRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "अमित, बताइए मैं आपकी क्या मदद करूँ?")
        }

        try {
            _recognizedText.value = ""
            _partialText.value = ""
            _errorMessage.value = null
            _status.value = SpeechStatus.LISTENING
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _status.value = SpeechStatus.ERROR
            _errorMessage.value = "Error starting listening: ${e.message}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _status.value = SpeechStatus.PROCESSING
        } catch (e: Exception) {
            _status.value = SpeechStatus.IDLE
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            _status.value = SpeechStatus.IDLE
            _partialText.value = ""
        } catch (e: Exception) {
            _status.value = SpeechStatus.IDLE
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
