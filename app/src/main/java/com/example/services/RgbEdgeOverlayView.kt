package com.example.services

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.data.RgbBrightness
import com.example.data.RgbSpeed
import com.example.ui.components.RgbState

/**
 * High-performance System Overlay View that renders the 4-sided clockwise running
 * RGB Edge Light laser around the entire device screen across all applications.
 */
class RgbEdgeOverlayView(context: Context) : View(context) {

    private var phase: Float = 0f
    private var isRgbActive: Boolean = true
    private var speed: RgbSpeed = RgbSpeed.NORMAL
    private var brightness: RgbBrightness = RgbBrightness.HIGH
    private var state: RgbState = RgbState.NORMAL

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null

    // Rainbow 360 loop colors
    private val rgbColors = intArrayOf(
        Color.parseColor("#EF4444"), // Red
        Color.parseColor("#F97316"), // Orange
        Color.parseColor("#EAB308"), // Yellow
        Color.parseColor("#22C55E"), // Green
        Color.parseColor("#22D3EE"), // Cyan
        Color.parseColor("#3B82F6"), // Blue
        Color.parseColor("#A855F7"), // Purple
        Color.parseColor("#EC4899"), // Magenta
        Color.parseColor("#EF4444")  // Red loop
    )

    private val colorPositions = floatArrayOf(
        0.0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1.0f
    )

    init {
        startAnimation()
    }

    fun setConfig(enabled: Boolean, newSpeed: RgbSpeed, newBrightness: RgbBrightness, newState: RgbState) {
        val speedChanged = this.speed != newSpeed
        this.isRgbActive = enabled
        this.speed = newSpeed
        this.brightness = newBrightness
        this.state = newState

        if (speedChanged) {
            startAnimation()
        }
        invalidate()
    }

    fun setState(newState: RgbState) {
        this.state = newState
        invalidate()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = speed.durationMs.toLong()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isRgbActive) return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val strokePx = brightness.strokeWidthDp * resources.displayMetrics.density
        val glowPx = strokePx * 2.8f
        val alphaMultiplier = brightness.alphaFactor
        val perimeter = 2f * (w + h)

        val cornerRadius = 36f * resources.displayMetrics.density
        val inset = strokePx / 2f
        val rect = RectF(inset, inset, w - inset, h - inset)

        when (state) {
            RgbState.LISTENING -> {
                // Vibrant Cyan Pulse for voice listening
                val cyanColor = Color.parseColor("#22D3EE")
                val pulseAlpha = (180 + (70 * kotlin.math.sin(phase * 2 * Math.PI.toFloat())).toInt()).coerceIn(50, 255)

                glowPaint.color = cyanColor
                glowPaint.alpha = (pulseAlpha * 0.4f * alphaMultiplier).toInt().coerceIn(0, 255)
                glowPaint.strokeWidth = glowPx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)

                strokePaint.shader = null
                strokePaint.color = cyanColor
                strokePaint.alpha = (pulseAlpha * alphaMultiplier).toInt().coerceIn(0, 255)
                strokePaint.strokeWidth = strokePx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            }

            RgbState.PROCESSING -> {
                // High-speed Magenta sweep
                val magentaColor = Color.parseColor("#EC4899")
                glowPaint.color = magentaColor
                glowPaint.alpha = (160 * alphaMultiplier).toInt().coerceIn(0, 255)
                glowPaint.strokeWidth = glowPx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)

                strokePaint.shader = null
                strokePaint.color = magentaColor
                strokePaint.alpha = (255 * alphaMultiplier).toInt().coerceIn(0, 255)
                strokePaint.strokeWidth = strokePx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            }

            RgbState.SUCCESS -> {
                val greenColor = Color.parseColor("#22C55E")
                glowPaint.color = greenColor
                glowPaint.alpha = (180 * alphaMultiplier).toInt().coerceIn(0, 255)
                glowPaint.strokeWidth = glowPx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)

                strokePaint.shader = null
                strokePaint.color = greenColor
                strokePaint.alpha = (255 * alphaMultiplier).toInt().coerceIn(0, 255)
                strokePaint.strokeWidth = strokePx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            }

            RgbState.ERROR -> {
                val redColor = Color.parseColor("#EF4444")
                glowPaint.color = redColor
                glowPaint.alpha = (180 * alphaMultiplier).toInt().coerceIn(0, 255)
                glowPaint.strokeWidth = glowPx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)

                strokePaint.shader = null
                strokePaint.color = redColor
                strokePaint.alpha = (255 * alphaMultiplier).toInt().coerceIn(0, 255)
                strokePaint.strokeWidth = strokePx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            }

            RgbState.NORMAL, RgbState.IDLE -> {
                // 1. Base Chromatic Edge Ambient Sweep
                val sweep = SweepGradient(w / 2f, h / 2f, rgbColors, colorPositions)
                val matrix = android.graphics.Matrix()
                matrix.postRotate(phase * 360f, w / 2f, h / 2f)
                sweep.setLocalMatrix(matrix)

                glowPaint.shader = sweep
                glowPaint.alpha = (120 * alphaMultiplier).toInt().coerceIn(0, 255)
                glowPaint.strokeWidth = glowPx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)

                strokePaint.shader = sweep
                strokePaint.alpha = (230 * alphaMultiplier).toInt().coerceIn(0, 255)
                strokePaint.strokeWidth = strokePx
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)

                // 2. Clockwise Traveling Laser Beams along the 4 edges
                drawClockwiseLaserHead(canvas, w, h, perimeter, phase, strokePx * 1.6f, Color.parseColor("#00F0FF"))
                drawClockwiseLaserHead(canvas, w, h, perimeter, (phase + 0.5f) % 1f, strokePx * 1.6f, Color.parseColor("#FF007F"))
            }
        }
    }

    private fun drawClockwiseLaserHead(
        canvas: Canvas,
        w: Float,
        h: Float,
        perimeter: Float,
        p: Float,
        radius: Float,
        colorInt: Int
    ) {
        val currentDist = p * perimeter
        var x = 0f
        var y = 0f

        when {
            currentDist < w -> {
                // Top Edge: Left to Right
                x = currentDist
                y = 0f
            }
            currentDist < w + h -> {
                // Right Edge: Top to Bottom
                x = w
                y = currentDist - w
            }
            currentDist < 2f * w + h -> {
                // Bottom Edge: Right to Left
                x = w - (currentDist - (w + h))
                y = h
            }
            else -> {
                // Left Edge: Bottom to Top
                x = 0f
                y = h - (currentDist - (2f * w + h))
            }
        }

        beamPaint.color = colorInt
        beamPaint.alpha = 240
        canvas.drawCircle(x, y, radius, beamPaint)

        // Bright white core
        beamPaint.color = Color.WHITE
        beamPaint.alpha = 255
        canvas.drawCircle(x, y, radius * 0.5f, beamPaint)
    }
}
