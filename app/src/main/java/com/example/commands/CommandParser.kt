package com.example.commands

object CommandParser {

    fun parse(rawInput: String): IntentType {
        val text = rawInput.trim()
        val lower = text.lowercase()

        // 1. Check for Multi-step action triggers like "खोलकर ... search करो" / "kholkar ... search karo"
        val multiStep = checkMultiStepPattern(text, lower)
        if (multiStep != null) return multiStep

        // 2. Notification reading: "notifications पढ़ो", "meri notifications", "read notifications", "नोटीफिकेशन पढ़ो"
        if (lower.contains("notification") || lower.contains("नोटिफिकेशन") || lower.contains("सूचनाएं") || lower.contains("notif")) {
            if (lower.contains("पढ़ो") || lower.contains("padho") || lower.contains("read") || lower.contains("batao") || lower.contains("सुनाओ")) {
                return IntentType.ReadNotifications
            }
        }

        // 3. Calling commands: "मम्मी को call करो", "call mummy", "अमित को फोन लगाओ", "bhai ko call karo"
        val callIntent = checkCallPattern(text, lower)
        if (callIntent != null) return callIntent

        // 4. Accessibility navigation: "back जाओ", "home जाओ", "ऊपर scroll karo", "नीचे scroll karo", "screen padho"
        if (lower == "back जाओ" || lower == "back jao" || lower == "go back" || lower == "पीछे जाओ" || lower == "back") {
            return IntentType.AccessibilityBack
        }
        if (lower == "home जाओ" || lower == "home jao" || lower == "go home" || lower == "होम जाओ" || lower == "home screen") {
            return IntentType.AccessibilityHome
        }
        if (lower.contains("ऊपर scroll") || lower.contains("upar scroll") || lower.contains("scroll up")) {
            return IntentType.AccessibilityScrollUp
        }
        if (lower.contains("नीचे scroll") || lower.contains("neeche scroll") || lower.contains("scroll down")) {
            return IntentType.AccessibilityScrollDown
        }
        if (lower.contains("स्क्रीन पढ़ो") || lower.contains("screen padho") || lower.contains("read screen")) {
            return IntentType.AccessibilityReadScreen
        }

        // 5. Accessibility Click & Type
        val clickIntent = checkClickPattern(text, lower)
        if (clickIntent != null) return clickIntent

        val typeIntent = checkTypePattern(text, lower)
        if (typeIntent != null) return typeIntent

        // 6. Device controls: Flashlight / Torch
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("टॉर्च") || lower.contains("फ्लैशलाइट")) {
            val isOff = lower.contains("off") || lower.contains("बंद") || lower.contains("band") || lower.contains("bujhao") || lower.contains("बुझाओ")
            return IntentType.TorchControl(!isOff)
        }

        // Volume control
        if (lower.contains("volume") || lower.contains("आवाज़") || lower.contains("sound") || lower.contains("awaz")) {
            if (lower.contains("बढ़ाओ") || lower.contains("badhao") || lower.contains("up") || lower.contains("increase") || lower.contains("tez") || lower.contains("तेज़")) {
                return IntentType.VolumeControl(increase = true)
            }
            if (lower.contains("कम") || lower.contains("kam") || lower.contains("down") || lower.contains("decrease") || lower.contains("ghatao")) {
                return IntentType.VolumeControl(increase = false)
            }
        }

        // Battery
        if (lower.contains("battery") || lower.contains("बैटरी") || lower.contains("चार्जिंग") || lower.contains("charging")) {
            return IntentType.BatteryStatus
        }

        // System Settings
        if (lower.contains("wi-fi") || lower.contains("wifi") || lower.contains("वाईफाई")) {
            return IntentType.OpenSettings("wifi")
        }
        if (lower.contains("bluetooth") || lower.contains("ब्लूटूथ")) {
            return IntentType.OpenSettings("bluetooth")
        }
        if (lower.contains("display setting") || lower.contains("brightness setting") || lower.contains("डिस्प्ले")) {
            return IntentType.OpenSettings("display")
        }
        if (lower.contains("accessibility setting") || lower.contains("एक्सेसिबिलिटी")) {
            return IntentType.OpenSettings("accessibility")
        }

        // 7. Dedicated Search
        if (lower.startsWith("youtube पर search करो") || lower.startsWith("youtube search")) {
            val query = lower.replace("youtube पर search करो", "").replace("youtube search", "").trim()
            return IntentType.YouTubeSearch(query)
        }
        if (lower.startsWith("google पर search करो") || lower.startsWith("google search")) {
            val query = lower.replace("google पर search करो", "").replace("google search", "").trim()
            return IntentType.GoogleSearch(query)
        }

        // 8. App Launching: "YouTube खोलो", "WhatsApp kholo", "Open Chrome", "Camera chalu karo"
        val openApp = checkAppOpenPattern(text, lower)
        if (openApp != null) return openApp

        // 9. Fallback to General AI Chat
        return IntentType.GeneralChat(text)
    }

    private fun checkMultiStepPattern(text: String, lower: String): IntentType? {
        // e.g. "YouTube खोलकर Amit Kumar search करो" or "YouTube khol kar Amit Kumar search karo"
        val delimiters = listOf("खोलकर", "kholkar", "khol kar", "open and", "खोल कर")
        for (delimiter in delimiters) {
            if (lower.contains(delimiter)) {
                val parts = text.split(Regex(delimiter, RegexOption.IGNORE_CASE))
                if (parts.size >= 2) {
                    val appPart = parts[0].replace("myra", "", ignoreCase = true).trim()
                    val nextPart = parts[1].trim()

                    // Check if second part is a search or click action
                    if (nextPart.lowercase().contains("search") || nextPart.lowercase().contains("सर्च") || nextPart.lowercase().contains("ढूंढो")) {
                        val searchQuery = nextPart
                            .replace("search करो", "", ignoreCase = true)
                            .replace("search karo", "", ignoreCase = true)
                            .replace("search", "", ignoreCase = true)
                            .replace("सर्च करो", "", ignoreCase = true)
                            .trim()

                        val steps = listOf(
                            ActionStep(1, "$appPart Application खोलना", "OPEN_APP", appPart),
                            ActionStep(2, "Application लोड होने की प्रतीक्षा करना", "WAIT", "1500"),
                            ActionStep(3, "Search बार खोजना", "FIND_NODE", "search"),
                            ActionStep(4, "Search बार पर क्लिक करना", "CLICK", "search"),
                            ActionStep(5, "\"$searchQuery\" टाइप करना", "TYPE", searchQuery),
                            ActionStep(6, "Search पूरा करना", "SUBMIT_SEARCH", searchQuery)
                        )
                        return IntentType.MultiStepPlan(
                            planSummary = "$appPart खोलकर '$searchQuery' search करना",
                            steps = steps
                        )
                    }
                }
            }
        }
        return null
    }

    private fun checkCallPattern(text: String, lower: String): IntentType? {
        // "मम्मी को call करो", "call mummy", "bhai ko phone lagao"
        val regexHindi = Regex("(.*)\\s*(?:को)?\\s*(?:call|कॉल|फोन|phone)\\s*(?:करो|लगाओ|karo|lagao)", RegexOption.IGNORE_CASE)
        val matchHindi = regexHindi.find(text)
        if (matchHindi != null) {
            val contact = matchHindi.groupValues[1]
                .replace("myra", "", ignoreCase = true)
                .replace("please", "", ignoreCase = true)
                .trim()
            if (contact.isNotBlank()) {
                return IntentType.CallContact(contact)
            }
        }

        if (lower.startsWith("call ") || lower.startsWith("कॉल ")) {
            val contact = text.substring(5).trim()
            if (contact.isNotBlank()) {
                return IntentType.CallContact(contact)
            }
        }

        return null
    }

    private fun checkClickPattern(text: String, lower: String): IntentType? {
        // "Search पर click करो", "इस button को दबाओ", "click on Search"
        val clickPatterns = listOf(
            Regex("(.*)\\s*(?:पर)?\\s*(?:click|क्लिक|press|दबाओ)\\s*(?:करो|karo)?", RegexOption.IGNORE_CASE),
            Regex("(?:click on|click)\\s+(.*)", RegexOption.IGNORE_CASE)
        )
        for (pattern in clickPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val target = match.groupValues[1]
                    .replace("myra", "", ignoreCase = true)
                    .replace("इस", "")
                    .replace("को", "")
                    .replace("button", "")
                    .replace("बटन", "")
                    .trim()
                if (target.isNotBlank()) {
                    return IntentType.AccessibilityClick(target)
                }
            }
        }
        return null
    }

    private fun checkTypePattern(text: String, lower: String): IntentType? {
        // "यहाँ Amit Kumar लिखो", "type Amit Kumar", "Amit Kumar type karo"
        val typePatterns = listOf(
            Regex("(?:यहाँ|yahan)?\\s*(.*)\\s*(?:लिखो|type करो|type karo)", RegexOption.IGNORE_CASE),
            Regex("(?:type)\\s+(.*)", RegexOption.IGNORE_CASE)
        )
        for (pattern in typePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val textToType = match.groupValues[1]
                    .replace("myra", "", ignoreCase = true)
                    .replace("यहाँ", "")
                    .replace("yahan", "")
                    .trim()
                if (textToType.isNotBlank()) {
                    return IntentType.AccessibilityType(textToType)
                }
            }
        }
        return null
    }

    private fun checkAppOpenPattern(text: String, lower: String): IntentType? {
        val openSuffixes = listOf("खोलो", "kholo", "khol do", "खोल दो", "चलाओ", "chalu karo", "chalao", "start karo", "start", "open")
        for (suffix in openSuffixes) {
            if (lower.endsWith(suffix)) {
                val appQuery = text.substring(0, text.length - suffix.length)
                    .replace("myra", "", ignoreCase = true)
                    .replace("please", "", ignoreCase = true)
                    .replace("app", "", ignoreCase = true)
                    .replace("ऐप", "", ignoreCase = true)
                    .trim()
                if (appQuery.isNotBlank()) {
                    return IntentType.OpenApp(appQuery)
                }
            }
            if (lower.startsWith(suffix)) {
                val appQuery = text.substring(suffix.length)
                    .replace("myra", "", ignoreCase = true)
                    .replace("please", "", ignoreCase = true)
                    .replace("app", "", ignoreCase = true)
                    .replace("ऐप", "", ignoreCase = true)
                    .trim()
                if (appQuery.isNotBlank()) {
                    return IntentType.OpenApp(appQuery)
                }
            }
        }
        return null
    }
}
