package com.example.oskolki.ar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.oskolki.R
import com.example.oskolki.model.Marker
import java.text.SimpleDateFormat
import java.util.Locale

class MarkerView(
    context: Context,
    private val marker: Marker,
    private val distance: Double
) : FrameLayout(context) {

    init {
        setupView()
    }

    private fun setupView() {
        val minSize = 40
        val maxSize = 150
        val clampedDistance = distance.coerceIn(0.0, 500.0)
        val iconSize = (maxSize - (clampedDistance / 500.0) * (maxSize - minSize)).toInt()

        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        val icon = ImageView(context).apply {
            layoutParams = LayoutParams(iconSize, (iconSize * 1.15).toInt())
            setImageDrawable(BitmapDrawable(context.resources, createHexagonBitmap(iconSize, marker.expiresAt)))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val fullText = marker.text ?: "Метка #${marker.id}"
        val labelText = if (fullText.length > 10) fullText.take(9) + "…" else fullText
        val textView = TextView(context).apply {
            layoutParams = LayoutParams(iconSize, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (iconSize * 1.15).toInt() + 6
            }
            text = "$labelText\n${"%.1f".format(distance)}м"
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        container.addView(icon)
        container.addView(textView)
        addView(container)
    }

    private fun createHexagonBitmap(size: Int, expiresAt: String?): Bitmap {
        val width = size
        val height = (size * 1.15).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val color = timeBasedColor(expiresAt)

        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val rx = width / 2f
        val ry = height / 2f

        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 90).toDouble())
            val px = cx + rx * Math.cos(angle).toFloat()
            val py = cy + ry * Math.sin(angle).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, fillPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawPath(path, strokePaint)

        return bitmap
    }

    private fun timeBasedColor(expiresAt: String?): Int {
        if (expiresAt == null) return Color.argb(220, 0, 200, 0)

        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val expires = fmt.parse(expiresAt)?.time ?: return Color.argb(220, 0, 200, 0)
            val now = System.currentTimeMillis()
            val remaining = expires - now
            val maxTime = 30L * 24 * 60 * 60 * 1000
            val ratio = (remaining.toFloat() / maxTime).coerceIn(0f, 1f)

            val h = 120f * ratio
            val s = 0.3f + 0.7f * ratio
            val v = 0.3f + 0.7f * ratio
            val alpha = (155 + 100 * ratio).toInt()
            val rgb = Color.HSVToColor(floatArrayOf(h, s, v))
            Color.argb(alpha.coerceIn(0, 255), Color.red(rgb), Color.green(rgb), Color.blue(rgb))
        } catch (_: Exception) {
            Color.argb(220, 0, 200, 0)
        }
    }
}
