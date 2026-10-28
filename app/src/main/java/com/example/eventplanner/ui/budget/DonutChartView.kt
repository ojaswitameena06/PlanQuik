package com.example.eventplanner.ui.budget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import kotlin.math.min

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val oval = RectF()

    private var slices: List<Pair<Float, Int>> = emptyList()
    private var valueText = ""
    private var labelText = ""

    init {
        trackPaint.color = MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorOutline, Color.LTGRAY
        )
        valuePaint.color = MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK
        )
        labelPaint.color = MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY
        )
    }

    fun setData(slices: List<Pair<Float, Int>>, valueText: String, labelText: String) {
        this.slices = slices
        this.valueText = valueText
        this.labelText = labelText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val stroke = size * 0.16f
        val left = (width - size) / 2f + stroke / 2f
        val top = (height - size) / 2f + stroke / 2f
        oval.set(left, top, left + size - stroke, top + size - stroke)

        trackPaint.strokeWidth = stroke
        arcPaint.strokeWidth = stroke
        canvas.drawArc(oval, 0f, 360f, false, trackPaint)

        val total = slices.sumOf { it.first.toDouble() }.toFloat()
        if (total > 0f) {
            var start = -90f
            val gap = if (slices.size > 1) 2f else 0f
            for ((value, color) in slices) {
                val sweep = value / total * 360f
                arcPaint.color = color
                canvas.drawArc(oval, start + gap / 2f, (sweep - gap).coerceAtLeast(0.5f), false, arcPaint)
                start += sweep
            }
        }

        // Centre text, shrunk to fit inside the ring
        val maxWidth = (size - stroke * 2) * 0.85f
        valuePaint.textSize = size * 0.15f
        val measured = valuePaint.measureText(valueText)
        if (measured > maxWidth) valuePaint.textSize = valuePaint.textSize * maxWidth / measured
        labelPaint.textSize = size * 0.1f

        val cx = width / 2f
        val cy = height / 2f
        canvas.drawText(valueText, cx, cy, valuePaint)
        canvas.drawText(labelText, cx, cy + labelPaint.textSize * 1.4f, labelPaint)
    }
}