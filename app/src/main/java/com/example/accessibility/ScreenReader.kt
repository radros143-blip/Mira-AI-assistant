package com.example.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object ScreenReader {

    fun readCurrentScreen(root: AccessibilityNodeInfo?): String {
        if (root == null) return "स्क्रीन पर कोई पठनीय टेक्स्ट नहीं मिला।"
        val texts = mutableListOf<String>()
        NodeFinder.collectAllVisibleTexts(root, texts)

        if (texts.isEmpty()) {
            return "स्क्रीन पर कोई पठनीय सामग्री नहीं मिली।"
        }

        // Limit to first 12 meaningful lines for natural voice readout
        val limited = texts.take(12)
        return "स्क्रीन पर दिखाई दे रहा है: " + limited.joinToString(separator = "। ")
    }
}
