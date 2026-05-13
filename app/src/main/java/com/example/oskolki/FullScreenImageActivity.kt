package com.example.oskolki

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var ivFullImage: ImageView
    private var scaleDetector: ScaleGestureDetector? = null
    private val matrix = Matrix()
    private val lastTouch = PointF()
    private var activePointerId = -1
    private var mode = NONE

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        private const val MAX_SCALE = 5f
        private const val MIN_SCALE = 0.5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageUrl = intent.getStringExtra("image_url") ?: run { finish(); return }

        ivFullImage = findViewById(R.id.iv_full_image)
        val btnClose = findViewById<ImageButton>(R.id.btn_close_full)

        ivFullImage.scaleType = ImageView.ScaleType.MATRIX

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    ivFullImage.setImageBitmap(resource)
                    ivFullImage.post { fitImageInView(resource.width, resource.height) }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        scaleDetector = ScaleGestureDetector(this, ScaleListener())

        ivFullImage.setOnTouchListener { _, event ->
            scaleDetector?.onTouchEvent(event)
            handleTouchEvent(event)
            true
        }

        btnClose.setOnClickListener { finish() }
    }

    private fun fitImageInView(bmpW: Int, bmpH: Int) {
        val viewW = ivFullImage.width
        val viewH = ivFullImage.height
        if (viewW <= 0 || viewH <= 0 || bmpW <= 0 || bmpH <= 0) return

        val scale = minOf(viewW.toFloat() / bmpW, viewH.toFloat() / bmpH)
        val dx = (viewW - bmpW * scale) / 2f
        val dy = (viewH - bmpH * scale) / 2f

        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)
        ivFullImage.imageMatrix = matrix
    }

    private fun handleTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = event.actionIndex
                lastTouch.x = event.getX(pointerIndex)
                lastTouch.y = event.getY(pointerIndex)
                activePointerId = event.getPointerId(pointerIndex)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    if (pointerIndex == -1) return
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)
                    val dx = x - lastTouch.x
                    val dy = y - lastTouch.y
                    matrix.postTranslate(dx, dy)
                    lastTouch.x = x
                    lastTouch.y = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount <= 1) {
                    mode = NONE
                    activePointerId = -1
                }
            }
        }

        ivFullImage.imageMatrix = matrix
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val currentScale = getCurrentScale()
            val clamped = (currentScale * scale).coerceIn(MIN_SCALE, MAX_SCALE)
            if (clamped / currentScale != 1f) {
                matrix.postScale(clamped / currentScale, clamped / currentScale,
                    detector.focusX, detector.focusY)
                ivFullImage.imageMatrix = matrix
            }
            return true
        }
    }

    private fun getCurrentScale(): Float {
        val values = FloatArray(9)
        matrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }
}
