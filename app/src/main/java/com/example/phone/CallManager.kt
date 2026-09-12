package com.example.phone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

data class CallRequest(
    val contactName: String,
    val phoneNumber: String
)

object CallManager {

    fun initiateCall(context: Context, phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
