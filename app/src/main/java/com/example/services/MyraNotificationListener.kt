package com.example.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReceivedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MyraNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListenerActive.value = true
        refreshNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListenerActive.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { extractAndStore(it) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            val key = "${it.packageName}_${it.id}"
            val current = _notificationsList.value.toMutableList()
            current.removeAll { item -> item.id == key }
            _notificationsList.value = current
        }
    }

    private fun refreshNotifications() {
        try {
            val active = activeNotifications ?: return
            val list = mutableListOf<ReceivedNotification>()
            for (sbn in active) {
                val item = parseNotification(sbn)
                if (item != null && item.title.isNotBlank()) {
                    list.add(item)
                }
            }
            _notificationsList.value = list
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun extractAndStore(sbn: StatusBarNotification) {
        val item = parseNotification(sbn) ?: return
        if (item.title.isBlank() && item.text.isBlank()) return

        val current = _notificationsList.value.toMutableList()
        current.removeAll { it.id == item.id }
        current.add(0, item)
        // Keep latest 25
        _notificationsList.value = current.take(25)
    }

    private fun parseNotification(sbn: StatusBarNotification): ReceivedNotification? {
        val extras = sbn.notification.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        // Exclude empty system noises
        if (title.isBlank() && text.isBlank()) return null

        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        return ReceivedNotification(
            id = "${sbn.packageName}_${sbn.id}",
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            timestamp = sbn.postTime
        )
    }

    companion object {
        private val _isListenerActive = MutableStateFlow(false)
        val isListenerActive: StateFlow<Boolean> = _isListenerActive.asStateFlow()

        private val _notificationsList = MutableStateFlow<List<ReceivedNotification>>(emptyList())
        val notificationsList: StateFlow<List<ReceivedNotification>> = _notificationsList.asStateFlow()

        fun isNotificationAccessGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
            return false
        }

        fun getLatestNotificationsSummary(limit: Int = 5): String {
            val list = _notificationsList.value
            if (list.isEmpty()) {
                return "अमित, अभी आपके पास कोई नई notification नहीं है।"
            }
            val recent = list.take(limit)
            val sb = StringBuilder("अमित, आपके पास ${list.size} notifications हैं:\n")
            recent.forEachIndexed { index, notif ->
                sb.append("${index + 1}. ${notif.appName}: ${notif.title}")
                if (notif.text.isNotBlank()) {
                    sb.append(" - ${notif.text}")
                }
                sb.append("\n")
            }
            return sb.toString().trim()
        }
    }
}
