package com.example.apps

import android.content.Context
import android.content.Intent
import android.net.Uri

object AppLauncher {

    sealed class LaunchResult {
        data class Success(val appName: String, val packageName: String) : LaunchResult()
        data class NotFound(val appQuery: String) : LaunchResult()
        data class Error(val message: String) : LaunchResult()
    }

    fun launchAppByQuery(context: Context, appQuery: String): LaunchResult {
        val packageName = AppResolver.resolveAppPackage(context, appQuery)
        if (packageName == null) {
            return LaunchResult.NotFound(appQuery)
        }

        return launchPackage(context, packageName)
    }

    fun launchPackage(context: Context, packageName: String): LaunchResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            return LaunchResult.NotFound(packageName)
        }

        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val name = pm.getApplicationLabel(appInfo).toString()
            LaunchResult.Success(appName = name, packageName = packageName)
        } catch (e: Exception) {
            LaunchResult.Error(e.localizedMessage ?: "Application launch failed")
        }
    }

    fun searchYouTube(context: Context, query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun searchGoogle(context: Context, query: String): Boolean {
        return try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
