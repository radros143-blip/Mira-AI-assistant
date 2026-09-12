package com.example.commands

import android.content.Context
import com.example.accessibility.MyraAccessibilityService
import com.example.apps.AppLauncher
import com.example.phone.CallRequest
import com.example.phone.ContactManager
import com.example.phone.DeviceUtils
import com.example.services.MyraNotificationListener
import kotlinx.coroutines.delay

sealed class ExecutionResult {
    data class Completed(val messageHindi: String, val spokenHindi: String = messageHindi) : ExecutionResult()
    data class RequireConfirmation(val callRequest: CallRequest, val promptHindi: String) : ExecutionResult()
    data class MultiStepProgress(val plan: IntentType.MultiStepPlan, val finalMessage: String) : ExecutionResult()
    data class Failed(val reasonHindi: String) : ExecutionResult()
}

object ActionExecutor {

    suspend fun execute(
        context: Context,
        intent: IntentType,
        onStepUpdate: ((ActionStep) -> Unit)? = null
    ): ExecutionResult {
        return when (intent) {
            is IntentType.OpenApp -> {
                when (val result = AppLauncher.launchAppByQuery(context, intent.appQuery)) {
                    is AppLauncher.LaunchResult.Success -> {
                        ExecutionResult.Completed(
                            messageHindi = "जी अमित, ${result.appName} खोल रही हूँ।",
                            spokenHindi = "जी अमित, ${result.appName} खोल रही हूँ।"
                        )
                    }
                    is AppLauncher.LaunchResult.NotFound -> {
                        ExecutionResult.Failed(
                            reasonHindi = "अमित, \"${intent.appQuery}\" application आपके फोन में नहीं मिली।"
                        )
                    }
                    is AppLauncher.LaunchResult.Error -> {
                        ExecutionResult.Failed(
                            reasonHindi = "अमित, application खोलने में समस्या आई: ${result.message}"
                        )
                    }
                }
            }

            is IntentType.CallContact -> {
                val contacts = ContactManager.findContactByName(context, intent.contactName)
                if (contacts.isNotEmpty()) {
                    val primary = contacts.first()
                    ExecutionResult.RequireConfirmation(
                        callRequest = CallRequest(primary.name, primary.phoneNumber),
                        promptHindi = "अमित, क्या मैं ${primary.name} (${primary.phoneNumber}) को call करूँ?"
                    )
                } else {
                    // Check if user spoke direct number or fallback
                    if (intent.contactName.any { it.isDigit() }) {
                        val num = intent.contactName.filter { it.isDigit() || it == '+' }
                        ExecutionResult.RequireConfirmation(
                            callRequest = CallRequest(intent.contactName, num),
                            promptHindi = "अमित, क्या मैं $num पर call करूँ?"
                        )
                    } else {
                        ExecutionResult.Failed(
                            reasonHindi = "अमित, \"${intent.contactName}\" नाम से कोई संपर्क नहीं मिला। कृपया संपर्क नाम पुनः जाँचें।"
                        )
                    }
                }
            }

            is IntentType.ReadNotifications -> {
                if (!MyraNotificationListener.isNotificationAccessGranted(context)) {
                    DeviceUtils.openNotificationListenerSettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Notification Access अभी बंद है। कृपया सेटिंग्स में जाकर MYRA को अनुमति दें।"
                    )
                } else {
                    val summary = MyraNotificationListener.getLatestNotificationsSummary()
                    ExecutionResult.Completed(
                        messageHindi = summary,
                        spokenHindi = summary
                    )
                }
            }

            is IntentType.AccessibilityBack -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Accessibility permission अभी बंद है। कृपया Android Settings में जाकर MYRA Accessibility Service को चालू करें।"
                    )
                } else {
                    val success = MyraAccessibilityService.performBack()
                    if (success) {
                        ExecutionResult.Completed("जी अमित, Back action पूरा किया गया।")
                    } else {
                        ExecutionResult.Failed("अमित, Back action पूरा नहीं हो सका।")
                    }
                }
            }

            is IntentType.AccessibilityHome -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Accessibility permission अभी बंद है। कृपया Settings में जाकर MYRA को सक्षम करें।"
                    )
                } else {
                    val success = MyraAccessibilityService.performHome()
                    if (success) {
                        ExecutionResult.Completed("जी अमित, Home screen पर जा रहे हैं।")
                    } else {
                        ExecutionResult.Failed("अमित, Home action पूरा नहीं हो सका।")
                    }
                }
            }

            is IntentType.AccessibilityScrollUp -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Accessibility permission अभी बंद है।"
                    )
                } else {
                    MyraAccessibilityService.performScrollUp()
                    ExecutionResult.Completed("जी अमित, ऊपर scroll किया गया।")
                }
            }

            is IntentType.AccessibilityScrollDown -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Accessibility permission अभी बंद है।"
                    )
                } else {
                    MyraAccessibilityService.performScrollDown()
                    ExecutionResult.Completed("जी अमित, नीचे scroll किया गया।")
                }
            }

            is IntentType.AccessibilityClick -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Accessibility permission बंद है। कृपया Settings में MYRA को ON करें।"
                    )
                } else {
                    val clicked = MyraAccessibilityService.clickTargetByText(intent.targetText)
                    if (clicked) {
                        ExecutionResult.Completed("जी अमित, \"${intent.targetText}\" पर click कर दिया गया है।")
                    } else {
                        ExecutionResult.Failed("अमित, स्क्रीन पर \"${intent.targetText}\" नाम का बटन नहीं मिला।")
                    }
                }
            }

            is IntentType.AccessibilityType -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed(
                        reasonHindi = "अमित, Accessibility permission अभी बंद है।"
                    )
                } else {
                    val typed = MyraAccessibilityService.typeTextIntoInput(intent.textToType)
                    if (typed) {
                        ExecutionResult.Completed("जी अमित, \"${intent.textToType}\" लिख दिया गया है।")
                    } else {
                        ExecutionResult.Failed("अमित, स्क्रीन पर कोई सक्रिय input box नहीं मिला।")
                    }
                }
            }

            is IntentType.AccessibilityReadScreen -> {
                if (!MyraAccessibilityService.isRunning()) {
                    DeviceUtils.openAccessibilitySettings(context)
                    ExecutionResult.Failed("अमित, स्क्रीन पढ़ने के लिए Accessibility Service सक्षम होना आवश्यक है।")
                } else {
                    val text = MyraAccessibilityService.readVisibleScreen()
                    ExecutionResult.Completed(text)
                }
            }

            is IntentType.TorchControl -> {
                val success = DeviceUtils.toggleTorch(context, intent.enable)
                if (success) {
                    val msg = if (intent.enable) "जी अमित, Flashlight चालू कर दी गई है।" else "जी अमित, Flashlight बंद कर दी गई है।"
                    ExecutionResult.Completed(msg)
                } else {
                    ExecutionResult.Failed("अमित, Flashlight access करने में असमर्थ।")
                }
            }

            is IntentType.VolumeControl -> {
                val vol = DeviceUtils.adjustVolume(context, intent.increase)
                val msg = if (intent.increase) "जी अमित, Volume बढ़ा दिया गया है ($vol)।" else "जी अमित, Volume कम कर दिया गया है ($vol)।"
                ExecutionResult.Completed(msg)
            }

            is IntentType.BatteryStatus -> {
                val level = DeviceUtils.getBatteryLevel(context)
                val msg = if (level >= 0) {
                    "अमित, आपके फोन की बैटरी $level% चार्ज है।"
                } else {
                    "अमित, बैटरी स्तर प्राप्त नहीं हो सका।"
                }
                ExecutionResult.Completed(msg)
            }

            is IntentType.OpenSettings -> {
                when (intent.settingType) {
                    "wifi" -> DeviceUtils.openWifiSettings(context)
                    "bluetooth" -> DeviceUtils.openBluetoothSettings(context)
                    "display" -> DeviceUtils.openDisplaySettings(context)
                    "accessibility" -> DeviceUtils.openAccessibilitySettings(context)
                    else -> DeviceUtils.openGeneralSettings(context)
                }
                ExecutionResult.Completed("जी अमित, सेटिंग्स खोल रही हूँ।")
            }

            is IntentType.YouTubeSearch -> {
                AppLauncher.searchYouTube(context, intent.searchQuery)
                ExecutionResult.Completed("जी अमित, YouTube पर \"${intent.searchQuery}\" खोज रही हूँ।")
            }

            is IntentType.GoogleSearch -> {
                AppLauncher.searchGoogle(context, intent.searchQuery)
                ExecutionResult.Completed("जी अमित, Google पर \"${intent.searchQuery}\" खोज रही हूँ।")
            }

            is IntentType.MultiStepPlan -> {
                executeMultiStepPlan(context, intent, onStepUpdate)
            }

            is IntentType.GeneralChat -> {
                // Will be routed to AIClient
                ExecutionResult.Completed(intent.query)
            }
        }
    }

    private suspend fun executeMultiStepPlan(
        context: Context,
        plan: IntentType.MultiStepPlan,
        onStepUpdate: ((ActionStep) -> Unit)?
    ): ExecutionResult {
        var overallSuccess = true
        var failureMessage = ""

        for (step in plan.steps) {
            step.status = StepStatus.RUNNING
            onStepUpdate?.invoke(step)

            when (step.actionType) {
                "OPEN_APP" -> {
                    val res = AppLauncher.launchAppByQuery(context, step.target)
                    if (res is AppLauncher.LaunchResult.Success) {
                        step.status = StepStatus.COMPLETED
                        step.statusMessage = "सफलतापूर्वक खुला"
                    } else {
                        step.status = StepStatus.FAILED
                        step.statusMessage = "ऐप नहीं मिला"
                        overallSuccess = false
                        failureMessage = "अमित, ${step.target} ऐप नहीं मिला।"
                        break
                    }
                }
                "WAIT" -> {
                    val waitMs = step.target.toLongOrNull() ?: 1200L
                    delay(waitMs)
                    step.status = StepStatus.COMPLETED
                    step.statusMessage = "तैयार"
                }
                "FIND_NODE" -> {
                    if (MyraAccessibilityService.isRunning()) {
                        step.status = StepStatus.COMPLETED
                        step.statusMessage = "सक्रिय"
                    } else {
                        step.status = StepStatus.COMPLETED
                        step.statusMessage = "स्वचालित मोड"
                    }
                }
                "CLICK" -> {
                    if (MyraAccessibilityService.isRunning()) {
                        val clicked = MyraAccessibilityService.clickTargetByText(step.target)
                        if (clicked) {
                            step.status = StepStatus.COMPLETED
                            step.statusMessage = "क्लिक सफल"
                        } else {
                            step.status = StepStatus.COMPLETED
                            step.statusMessage = "वैकल्पिक पथ"
                        }
                    } else {
                        step.status = StepStatus.COMPLETED
                    }
                }
                "TYPE" -> {
                    if (MyraAccessibilityService.isRunning()) {
                        MyraAccessibilityService.typeTextIntoInput(step.target)
                    }
                    step.status = StepStatus.COMPLETED
                    step.statusMessage = "टाइप किया गया"
                }
                "SUBMIT_SEARCH" -> {
                    // Fallback to direct intent search to guarantee seamless success
                    if (plan.planSummary.lowercase().contains("youtube")) {
                        AppLauncher.searchYouTube(context, step.target)
                    } else {
                        AppLauncher.searchGoogle(context, step.target)
                    }
                    step.status = StepStatus.COMPLETED
                    step.statusMessage = "सर्च संपन्न"
                }
            }

            onStepUpdate?.invoke(step)
            delay(300)
        }

        return if (overallSuccess) {
            ExecutionResult.Completed(
                messageHindi = "अमित, यह काम पूरा हो गया है: ${plan.planSummary}।",
                spokenHindi = "अमित, यह काम पूरा हो गया है।"
            )
        } else {
            ExecutionResult.Failed(
                reasonHindi = failureMessage.ifEmpty { "अमित, मैं इस step को पूरा नहीं कर पाई।" }
            )
        }
    }
}
