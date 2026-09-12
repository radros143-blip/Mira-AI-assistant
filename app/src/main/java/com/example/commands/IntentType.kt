package com.example.commands

sealed class IntentType {
    data class OpenApp(val appQuery: String) : IntentType()
    data class CallContact(val contactName: String) : IntentType()
    object ReadNotifications : IntentType()
    
    // Accessibility Commands
    object AccessibilityBack : IntentType()
    object AccessibilityHome : IntentType()
    object AccessibilityScrollUp : IntentType()
    object AccessibilityScrollDown : IntentType()
    data class AccessibilityClick(val targetText: String) : IntentType()
    data class AccessibilityType(val textToType: String) : IntentType()
    object AccessibilityReadScreen : IntentType()

    // Device Controls
    data class TorchControl(val enable: Boolean) : IntentType()
    data class VolumeControl(val increase: Boolean) : IntentType()
    object BatteryStatus : IntentType()
    data class OpenSettings(val settingType: String) : IntentType()

    // Web / Media Searches
    data class YouTubeSearch(val searchQuery: String) : IntentType()
    data class GoogleSearch(val searchQuery: String) : IntentType()

    // Multi-step Action
    data class MultiStepPlan(val planSummary: String, val steps: List<ActionStep>) : IntentType()

    // General AI Chat / Question
    data class GeneralChat(val query: String) : IntentType()
}

data class ActionStep(
    val stepIndex: Int,
    val titleHindi: String,
    val actionType: String,
    val target: String,
    var status: StepStatus = StepStatus.PENDING,
    var statusMessage: String = ""
)

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
