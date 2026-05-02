package com.example.oskolki.ar

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.oskolki.R
import com.example.oskolki.model.Marker

class MarkerView(
    context: Context,
    private val marker: Marker,
    private val distance: Double
) : FrameLayout(context) {

    init {
        setupView()
    }

    private fun setupView() {
        // Размер иконки: 50px на 50м, 500px на 0м
        val minSize = 50
        val maxSize = 500
        val clampedDistance = distance.coerceIn(0.0, 50.0)
        val iconSize = (maxSize - (clampedDistance / 50.0) * (maxSize - minSize)).toInt()

        // Создаем контейнер для метки
        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        // Иконка метки с динамическим размером
        val icon = ImageView(context).apply {
            layoutParams = LayoutParams(iconSize, iconSize)
            setBackgroundColor(getColorForType(1))
        }

        // Текст с названием и расстоянием
        val textView = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = iconSize + 10
            }
            text = "${marker.text ?: "Метка #${marker.id}"}\n${"%.1f".format(distance)}м"
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        container.addView(icon)
        container.addView(textView)
        addView(container)
    }

    private fun getColorForType(type: Int): Int {
        return when (type) {
            1 -> Color.argb(255, 0, 200, 0)     // Зеленый (новая)
            2 -> Color.argb(255, 255, 165, 0)   // Оранжевый
            3 -> Color.argb(255, 200, 0, 0)     // Красный (старая)
            else -> Color.GRAY
        }
    }
}
