package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build

object GestureController {

    fun performScrollUp(service: AccessibilityService, callback: ((Boolean) -> Unit)? = null) {
        val displayMetrics = service.resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()

        val startX = width / 2f
        val startY = height * 0.75f
        val endX = width / 2f
        val endY = height * 0.25f

        performSwipe(service, startX, startY, endX, endY, 300, callback)
    }

    fun performScrollDown(service: AccessibilityService, callback: ((Boolean) -> Unit)? = null) {
        val displayMetrics = service.resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()

        val startX = width / 2f
        val startY = height * 0.25f
        val endX = width / 2f
        val endY = height * 0.75f

        performSwipe(service, startX, startY, endX, endY, 300, callback)
    }

    fun performSwipe(
        service: AccessibilityService,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300,
        callback: ((Boolean) -> Unit)? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            service.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        callback?.invoke(true)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        callback?.invoke(false)
                    }
                },
                null
            )
        } else {
            callback?.invoke(false)
        }
    }

    fun performTap(
        service: AccessibilityService,
        x: Float,
        y: Float,
        callback: ((Boolean) -> Unit)? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 50)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            service.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        callback?.invoke(true)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        callback?.invoke(false)
                    }
                },
                null
            )
        } else {
            callback?.invoke(false)
        }
    }
}
