package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.MyraAccessibilityService
import com.example.ai.AIClient
import com.example.commands.ActionExecutor
import com.example.commands.ActionStep
import com.example.commands.CommandParser
import com.example.commands.ExecutionResult
import com.example.commands.IntentType
import com.example.data.MyraPreferences
import com.example.data.RgbBrightness
import com.example.data.RgbSpeed
import com.example.data.database.ChatMessageEntity
import com.example.data.database.CommandHistoryEntity
import com.example.data.database.MyraDatabase
import com.example.phone.CallManager
import com.example.phone.CallRequest
import com.example.phone.DeviceUtils
import com.example.services.MyraNotificationListener
import com.example.ui.components.MyraScreen
import com.example.ui.components.RgbState
import com.example.voice.SpeechRecognizerManager
import com.example.voice.SpeechStatus
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = MyraPreferences(application)
    private val database = MyraDatabase.getDatabase(application)
    private val commandHistoryDao = database.commandHistoryDao()
    private val chatMessageDao = database.chatMessageDao()

    val speechRecognizer = SpeechRecognizerManager(application)
    val textToSpeech = TextToSpeechManager(application)

    val commandHistory = commandHistoryDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages = chatMessageDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(MyraScreen.HOME)
    val currentScreen: StateFlow<MyraScreen> = _currentScreen.asStateFlow()

    private val _rgbState = MutableStateFlow(RgbState.NORMAL)
    val rgbState: StateFlow<RgbState> = _rgbState.asStateFlow()

    private val _lastAssistantResponse = MutableStateFlow("नमस्ते अमित! मैं MYRA हूँ। बताइए, मैं आपकी क्या मदद करूँ?")
    val lastAssistantResponse: StateFlow<String> = _lastAssistantResponse.asStateFlow()

    private val _pendingCallRequest = MutableStateFlow<CallRequest?>(null)
    val pendingCallRequest: StateFlow<CallRequest?> = _pendingCallRequest.asStateFlow()

    private val _activeMultiStepPlan = MutableStateFlow<IntentType.MultiStepPlan?>(null)
    val activeMultiStepPlan: StateFlow<IntentType.MultiStepPlan?> = _activeMultiStepPlan.asStateFlow()

    private val _isChatAiThinking = MutableStateFlow(false)
    val isChatAiThinking: StateFlow<Boolean> = _isChatAiThinking.asStateFlow()

    // Observable setting states
    val rgbEnabledState = MutableStateFlow(prefs.rgbEnabled)
    val rgbSpeedState = MutableStateFlow(prefs.rgbSpeed)
    val rgbBrightnessState = MutableStateFlow(prefs.rgbBrightness)
    val bgGlowState = MutableStateFlow(prefs.backgroundGlowEnabled)
    val visualMode24hState = MutableStateFlow(prefs.visualMode24h)

    val speakResponsesState = MutableStateFlow(prefs.speakResponses)
    val voiceSpeedState = MutableStateFlow(prefs.voiceSpeed)
    val voicePitchState = MutableStateFlow(prefs.voicePitch)
    val floatingBubbleState = MutableStateFlow(prefs.floatingBubbleEnabled)
    val backgroundActiveState = MutableStateFlow(prefs.backgroundActiveEnabled)

    val isTorchActiveState = MutableStateFlow(false)

    init {
        speechRecognizer.onResultListener = { recognizedQuery ->
            processCommand(recognizedQuery)
        }

        textToSpeech.setSpeechParameters(prefs.voicePitch, prefs.voiceSpeed)
        textToSpeech.configureLanguage("hi-IN")

        // Start Foreground Service for continuous background execution and overlay
        try {
            com.example.services.MyraForegroundService.startService(application)
        } catch (e: Exception) {
            // Ignore
        }

        // Listen to speech recognizer status to adjust RGB visual edge light
        viewModelScope.launch {
            speechRecognizer.status.collect { status ->
                when (status) {
                    SpeechStatus.LISTENING -> {
                        _rgbState.value = RgbState.LISTENING
                        com.example.services.MyraForegroundService.updateOverlayState(RgbState.LISTENING)
                    }
                    SpeechStatus.PROCESSING -> {
                        _rgbState.value = RgbState.PROCESSING
                        com.example.services.MyraForegroundService.updateOverlayState(RgbState.PROCESSING)
                    }
                    SpeechStatus.ERROR -> {
                        _rgbState.value = RgbState.ERROR
                        com.example.services.MyraForegroundService.updateOverlayState(RgbState.ERROR)
                        delay(1200)
                        _rgbState.value = RgbState.NORMAL
                        com.example.services.MyraForegroundService.updateOverlayState(RgbState.NORMAL)
                    }
                    SpeechStatus.IDLE -> {
                        if (_rgbState.value != RgbState.SUCCESS && _rgbState.value != RgbState.ERROR) {
                            _rgbState.value = RgbState.NORMAL
                            com.example.services.MyraForegroundService.updateOverlayState(RgbState.NORMAL)
                        }
                    }
                }
            }
        }
    }

    fun playStartupGreeting() {
        if (prefs.speakResponses) {
            textToSpeech.speak("नमस्ते अमित! मैं MYRA हूँ। बताइए, मैं आपकी क्या मदद करूँ?")
        }
    }

    fun setScreen(screen: MyraScreen) {
        _currentScreen.value = screen
    }

    fun toggleVoiceListening() {
        if (speechRecognizer.status.value == SpeechStatus.LISTENING) {
            speechRecognizer.stopListening()
        } else {
            textToSpeech.stop()
            speechRecognizer.startListening(prefs.voiceLanguage)
        }
    }

    fun processCommand(input: String) {
        if (input.isBlank()) return

        viewModelScope.launch {
            val intent = CommandParser.parse(input)
            _rgbState.value = RgbState.PROCESSING

            // Save user query in chat
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    sender = "USER",
                    message = input
                )
            )

            when (intent) {
                is IntentType.GeneralChat -> {
                    _isChatAiThinking.value = true
                    val aiResponse = AIClient.getResponse(intent.query)
                    _isChatAiThinking.value = false

                    _lastAssistantResponse.value = aiResponse
                    chatMessageDao.insertMessage(
                        ChatMessageEntity(
                            sender = "MYRA",
                            message = aiResponse
                        )
                    )

                    commandHistoryDao.insertHistory(
                        CommandHistoryEntity(
                            queryText = input,
                            intentType = "GENERAL_CHAT",
                            responseText = aiResponse,
                            isSuccess = true
                        )
                    )

                    if (prefs.speakResponses) {
                        textToSpeech.speak(aiResponse)
                    }

                    _rgbState.value = RgbState.SUCCESS
                    delay(1200)
                    _rgbState.value = RgbState.NORMAL
                }

                is IntentType.MultiStepPlan -> {
                    _activeMultiStepPlan.value = intent
                    val executionResult = ActionExecutor.execute(
                        context = getApplication(),
                        intent = intent,
                        onStepUpdate = { updatedStep ->
                            // Trigger state recomposition
                            val currentPlan = _activeMultiStepPlan.value
                            if (currentPlan != null) {
                                val updatedList = currentPlan.steps.map {
                                    if (it.stepIndex == updatedStep.stepIndex) updatedStep else it
                                }
                                _activeMultiStepPlan.value = currentPlan.copy(steps = updatedList)
                            }
                        }
                    )

                    handleExecutionResult(input, "MULTI_STEP", executionResult)
                }

                else -> {
                    val executionResult = ActionExecutor.execute(
                        context = getApplication(),
                        intent = intent
                    )
                    handleExecutionResult(input, intent.javaClass.simpleName, executionResult)
                }
            }
        }
    }

    private suspend fun handleExecutionResult(
        query: String,
        intentName: String,
        result: ExecutionResult
    ) {
        when (result) {
            is ExecutionResult.Completed -> {
                _lastAssistantResponse.value = result.messageHindi
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        sender = "MYRA",
                        message = result.messageHindi
                    )
                )
                commandHistoryDao.insertHistory(
                    CommandHistoryEntity(
                        queryText = query,
                        intentType = intentName,
                        responseText = result.messageHindi,
                        isSuccess = true
                    )
                )

                if (prefs.speakResponses) {
                    textToSpeech.speak(result.spokenHindi)
                }

                _rgbState.value = RgbState.SUCCESS
                delay(1200)
                _rgbState.value = RgbState.NORMAL
            }

            is ExecutionResult.RequireConfirmation -> {
                _pendingCallRequest.value = result.callRequest
                _lastAssistantResponse.value = result.promptHindi

                if (prefs.speakResponses) {
                    textToSpeech.speak(result.promptHindi)
                }

                _rgbState.value = RgbState.NORMAL
            }

            is ExecutionResult.Failed -> {
                _lastAssistantResponse.value = result.reasonHindi
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        sender = "MYRA",
                        message = result.reasonHindi
                    )
                )
                commandHistoryDao.insertHistory(
                    CommandHistoryEntity(
                        queryText = query,
                        intentType = intentName,
                        responseText = result.reasonHindi,
                        isSuccess = false
                    )
                )

                if (prefs.speakResponses) {
                    textToSpeech.speak(result.reasonHindi)
                }

                _rgbState.value = RgbState.ERROR
                delay(1200)
                _rgbState.value = RgbState.NORMAL
            }

            is ExecutionResult.MultiStepProgress -> {
                _lastAssistantResponse.value = result.finalMessage
            }
        }
    }

    fun confirmCall() {
        val req = _pendingCallRequest.value ?: return
        _pendingCallRequest.value = null
        val success = CallManager.initiateCall(getApplication(), req.phoneNumber)
        val msg = if (success) {
            "जी अमित, ${req.contactName} को call मिलाई जा रही है।"
        } else {
            "अमित, call करने में समस्या आई।"
        }
        _lastAssistantResponse.value = msg
        if (prefs.speakResponses) {
            textToSpeech.speak(msg)
        }
    }

    fun cancelCall() {
        _pendingCallRequest.value = null
        val msg = "जी अमित, कॉल रद्द कर दी गई है।"
        _lastAssistantResponse.value = msg
        if (prefs.speakResponses) {
            textToSpeech.speak(msg)
        }
    }

    fun speakText(text: String) {
        textToSpeech.speak(text)
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatMessageDao.clearAllMessages()
            commandHistoryDao.clearHistory()
            _lastAssistantResponse.value = "नमस्ते अमित! चैट इतिहास साफ़ कर दिया गया है।"
        }
    }

    // Toggle Torch
    fun toggleTorchDirectly() {
        val newTorch = !isTorchActiveState.value
        val res = DeviceUtils.toggleTorch(getApplication(), newTorch)
        if (res) {
            isTorchActiveState.value = newTorch
            val msg = if (newTorch) "जी अमित, Flashlight चालू कर दी गई है।" else "जी अमित, Flashlight बंद कर दी गई है।"
            _lastAssistantResponse.value = msg
            if (prefs.speakResponses) textToSpeech.speak(msg)
        }
    }

    // Settings Updaters
    fun updateRgbEnabled(enabled: Boolean) {
        prefs.rgbEnabled = enabled
        rgbEnabledState.value = enabled
        com.example.services.MyraForegroundService.updateConfig(getApplication())
    }

    fun updateRgbSpeed(speed: RgbSpeed) {
        prefs.rgbSpeed = speed
        rgbSpeedState.value = speed
        com.example.services.MyraForegroundService.updateConfig(getApplication())
    }

    fun updateRgbBrightness(brightness: RgbBrightness) {
        prefs.rgbBrightness = brightness
        rgbBrightnessState.value = brightness
        com.example.services.MyraForegroundService.updateConfig(getApplication())
    }

    fun updateBackgroundGlow(enabled: Boolean) {
        prefs.backgroundGlowEnabled = enabled
        bgGlowState.value = enabled
    }

    fun updateVisualMode24h(enabled: Boolean) {
        prefs.visualMode24h = enabled
        visualMode24hState.value = enabled
    }

    fun updateSpeakResponses(enabled: Boolean) {
        prefs.speakResponses = enabled
        speakResponsesState.value = enabled
    }

    fun updateVoiceSpeed(speed: Float) {
        prefs.voiceSpeed = speed
        voiceSpeedState.value = speed
        textToSpeech.setSpeechParameters(prefs.voicePitch, speed)
    }

    fun updateVoicePitch(pitch: Float) {
        prefs.voicePitch = pitch
        voicePitchState.value = pitch
        textToSpeech.setSpeechParameters(pitch, prefs.voiceSpeed)
    }

    fun updateFloatingBubbleEnabled(enabled: Boolean) {
        prefs.floatingBubbleEnabled = enabled
        floatingBubbleState.value = enabled
        com.example.services.MyraForegroundService.updateConfig(getApplication())
    }

    fun updateBackgroundActiveEnabled(enabled: Boolean) {
        prefs.backgroundActiveEnabled = enabled
        backgroundActiveState.value = enabled
        if (enabled) {
            com.example.services.MyraForegroundService.startService(getApplication())
        } else {
            com.example.services.MyraForegroundService.stopService(getApplication())
        }
    }

    fun restartBackgroundService() {
        com.example.services.MyraForegroundService.startService(getApplication())
        com.example.services.MyraForegroundService.updateConfig(getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        textToSpeech.shutdown()
    }
}
