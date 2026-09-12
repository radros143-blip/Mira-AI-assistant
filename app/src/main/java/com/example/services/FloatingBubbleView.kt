package com.example.services

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import kotlin.math.abs
import kotlin.math.hypot

@SuppressLint("ViewConstructor")
class FloatingBubbleView(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val onClickListener: () -> Unit
) : View(context) {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val touchSlop = 10f

    private var rotationAngle = 0f
    private val animator: ValueAnimator

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#18181B")
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#22D3EE")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val rainbowColors = intArrayOf(
        Color.parseColor("#22D3EE"), // Cyan
        Color.parseColor("#A855F7"), // Purple
        Color.parseColor("#EC4899"), // Magenta
        Color.parseColor("#EF4444"), // Red
        Color.parseColor("#EAB308"), // Yellow
        Color.parseColor("#22C55E"), // Green
        Color.parseColor("#22D3EE")  // Cyan
    )

    init {
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                rotationAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val cx = w / 2f
        val cy = h / 2f
        val radius = (w / 2f) - (6f * resources.displayMetrics.density)

        // 1. Soft Ambient Outer Radial Glow
        glowPaint.shader = RadialGradient(
            cx, cy, w / 2f,
            intArrayOf(Color.parseColor("#4422D3EE"), Color.parseColor("#22A855F7"), Color.TRANSPARENT),
            floatArrayOf(0.4f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, w / 2f, glowPaint)

        // 2. Dark Obsidian Circular Core
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // 3. Rotating RGB Edge Ring
        val sweep = SweepGradient(cx, cy, rainbowColors, null)
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationAngle, cx, cy)
        sweep.setLocalMatrix(matrix)

        ringPaint.shader = sweep
        ringPaint.strokeWidth = 3f * resources.displayMetrics.density
        canvas.drawCircle(cx, cy, radius, ringPaint)

        // 4. Center Mic / MYRA Icon (🎙️ or "M")
        val fontMetrics = textPaint.fontMetrics
        val textY = cy - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText("🎙️", cx, textY, textPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                if (hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                    isDragging = true
                }
                if (isDragging) {
                    layoutParams.x = initialX + dx.toInt()
                    layoutParams.y = initialY + dy.toInt()
                    try {
                        windowManager.updateViewLayout(this, layoutParams)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    // Click action
                    onClickListener()
                } else {
                    // Snap to closest screen edge
                    val screenWidth = resources.displayMetrics.widthPixels
                    val targetX = if (layoutParams.x + (width / 2) < screenWidth / 2) {
                        10
                    } else {
                        screenWidth - width - 10
                    }
                    layoutParams.x = targetX
                    try {
                        windowManager.updateViewLayout(this, layoutParams)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
