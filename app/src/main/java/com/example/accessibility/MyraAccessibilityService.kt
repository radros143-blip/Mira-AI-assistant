package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyraAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Track active window / view transitions if needed
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceActive.value = false
        }
    }

    companion object {
        private var instance: MyraAccessibilityService? = null
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun isRunning(): Boolean = instance != null

        fun isAccessibilitySettingsEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${MyraAccessibilityService::class.java.canonicalName}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                if (componentNameString.equals(expectedServiceName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        fun performBack(): Boolean {
            val s = instance ?: return false
            return s.performGlobalAction(GLOBAL_ACTION_BACK)
        }

        fun performHome(): Boolean {
            val s = instance ?: return false
            return s.performGlobalAction(GLOBAL_ACTION_HOME)
        }

        fun performNotifications(): Boolean {
            val s = instance ?: return false
            return s.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }

        fun performScrollUp(callback: ((Boolean) -> Unit)? = null): Boolean {
            val s = instance ?: return false
            GestureController.performScrollUp(s, callback)
            return true
        }

        fun performScrollDown(callback: ((Boolean) -> Unit)? = null): Boolean {
            val s = instance ?: return false
            GestureController.performScrollDown(s, callback)
            return true
        }

        fun clickTargetByText(targetText: String): Boolean {
            val s = instance ?: return false
            val root = s.rootInActiveWindow ?: return false
            val targetNode = NodeFinder.findNodeByText(root, targetText, clickOnly = true)
                ?: NodeFinder.findNodeByText(root, targetText, clickOnly = false)

            return if (targetNode != null) {
                val clicked = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (!clicked) {
                    val parent = NodeFinder.findClickableParent(targetNode)
                    parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
                } else {
                    true
                }
            } else {
                false
            }
        }

        fun typeTextIntoInput(textToType: String): Boolean {
            val s = instance ?: return false
            val root = s.rootInActiveWindow ?: return false
            val target = NodeFinder.findFocusedEditableNode(root)
                ?: NodeFinder.findFirstEditableNode(root)
                ?: return false

            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            return target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }

        fun readVisibleScreen(): String {
            val s = instance ?: return "Accessibility Service सक्रिय नहीं है।"
            val root = s.rootInActiveWindow ?: return "वर्तमान स्क्रीन पर कोई सामग्री नहीं मिली।"
            return ScreenReader.readCurrentScreen(root)
        }
    }
}
