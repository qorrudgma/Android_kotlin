package com.example.android

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class QRScannerOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val backgroundPaint = Paint().apply {
        color = "#80000000".toColorInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // 중앙 네모 크기
        val boxSize = width * 0.7f

        val left = (width - boxSize) / 2
        val top = (height - boxSize) / 2
        val right = left + boxSize
        val bottom = top + boxSize

        // 위쪽 어둡게
        canvas.drawRect(0f, 0f, width, top, backgroundPaint)

        // 아래쪽
        canvas.drawRect(0f, bottom, width, height, backgroundPaint)

        // 왼쪽
        canvas.drawRect(0f, top, left, bottom, backgroundPaint)

        // 오른쪽
        canvas.drawRect(right, top, width, bottom, backgroundPaint)

        // 네모 테두리
        canvas.drawRect(left, top, right, bottom, boxPaint)
    }
}