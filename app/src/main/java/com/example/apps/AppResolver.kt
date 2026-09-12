package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable? = null
)

object AppResolver {

    private val commonPackageMap = mapOf(
        "youtube" to listOf("com.google.android.youtube"),
        "yt" to listOf("com.google.android.youtube"),
        "यूट्यूब" to listOf("com.google.android.youtube"),
        "युट्यूब" to listOf("com.google.android.youtube"),
        "whatsapp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "व्हाट्सएप" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "वाट्सएप" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "instagram" to listOf("com.instagram.android"),
        "इंस्टाग्राम" to listOf("com.instagram.android"),
        "insta" to listOf("com.instagram.android"),
        "chrome" to listOf("com.android.chrome"),
        "क्रोम" to listOf("com.android.chrome"),
        "camera" to listOf("com.google.android.GoogleCamera", "com.android.camera", "com.sec.android.app.camera"),
        "कैमरा" to listOf("com.google.android.GoogleCamera", "com.android.camera", "com.sec.android.app.camera"),
        "gallery" to listOf("com.google.android.apps.photos", "com.android.gallery3d", "com.sec.android.gallery3d"),
        "गैलरी" to listOf("com.google.android.apps.photos", "com.android.gallery3d", "com.sec.android.gallery3d"),
        "photos" to listOf("com.google.android.apps.photos"),
        "फ़ोटो" to listOf("com.google.android.apps.photos"),
        "maps" to listOf("com.google.android.apps.maps"),
        "मैप्स" to listOf("com.google.android.apps.maps"),
        "गूगल मैप्स" to listOf("com.google.android.apps.maps"),
        "settings" to listOf("com.android.settings"),
        "सेटिंग्स" to listOf("com.android.settings"),
        "calculator" to listOf("com.google.android.calculator", "com.android.calculator2", "com.sec.android.app.popupcalculator"),
        "कैलकुलेटर" to listOf("com.google.android.calculator", "com.android.calculator2"),
        "contacts" to listOf("com.google.android.contacts", "com.android.contacts"),
        "कॉन्टैक्ट्स" to listOf("com.google.android.contacts", "com.android.contacts"),
        "phone" to listOf("com.google.android.dialer", "com.android.dialer", "com.samsung.android.dialer"),
        "फोन" to listOf("com.google.android.dialer", "com.android.dialer"),
        "dialer" to listOf("com.google.android.dialer", "com.android.dialer"),
        "dial" to listOf("com.google.android.dialer", "com.android.dialer"),
        "play store" to listOf("com.android.vending"),
        "प्ले स्टोर" to listOf("com.android.vending"),
        "telegram" to listOf("org.telegram.messenger"),
        "टेलीग्राम" to listOf("org.telegram.messenger"),
        "spotify" to listOf("com.spotify.music"),
        "स्पॉटिफ़ाई" to listOf("com.spotify.music"),
        "gmail" to listOf("com.google.android.gm"),
        "जीमेल" to listOf("com.google.android.gm")
    )

    fun resolveAppPackage(context: Context, appQuery: String): String? {
        val cleanQuery = appQuery.trim().lowercase()
        val pm = context.packageManager

        // 1. Direct match in common dictionary
        commonPackageMap[cleanQuery]?.let { candidates ->
            for (pkg in candidates) {
                if (isPackageInstalled(pm, pkg)) {
                    return pkg
                }
            }
        }

        // 2. Partial key match in common dictionary
        for ((key, pkgList) in commonPackageMap) {
            if (cleanQuery.contains(key) || key.contains(cleanQuery)) {
                for (pkg in pkgList) {
                    if (isPackageInstalled(pm, pkg)) {
                        return pkg
                    }
                }
            }
        }

        // 3. Scan all installed launcher apps dynamically
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            val packageName = info.activityInfo.packageName
            if (label.contains(cleanQuery) || cleanQuery.contains(label)) {
                return packageName
            }
        }

        return null
    }

    fun getAllInstalledLauncherApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val list = mutableListOf<InstalledAppInfo>()
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString()
            val packageName = info.activityInfo.packageName
            val icon = info.loadIcon(pm)
            list.add(InstalledAppInfo(label, packageName, icon))
        }
        return list.sortedBy { it.appName.lowercase() }
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }
}
