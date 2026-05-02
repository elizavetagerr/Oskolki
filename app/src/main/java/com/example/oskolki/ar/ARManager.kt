package com.example.oskolki.ar

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.oskolki.ReviewActivity
import com.example.oskolki.model.Marker
import java.util.concurrent.Executors

class ARManager(
    private val context: Context,
    private val container: FrameLayout
) {
    private val markers = mutableListOf<Marker>()
    private val markerViews = mutableMapOf<String, MarkerView>()
    private var currentAzimuth: Float = 0f
    private var currentLocation: android.location.Location? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun loadMarkers(markers: List<Marker>) {
        this.markers.clear()
        this.markers.addAll(markers)
    }

    fun updateMarkers(location: android.location.Location, azimuth: Float) {
        currentLocation = location
        currentAzimuth = azimuth

        executor.execute {
            updateMarkerViews()
        }
    }

    private fun updateMarkerViews() {
        if (currentLocation == null) return

        container.post {
            markerViews.values.forEach {
                container.removeView(it)
            }
            markerViews.clear()

            markers.forEach { marker ->
                val distance = MathUtils.calculateDistance(currentLocation!!, marker)

                if (distance <= 500f) {
                    val markerAzimuth = MathUtils.calculateAzimuth(currentLocation!!, marker)
                    val diff = MathUtils.calculateAzimuthDifference(markerAzimuth, currentAzimuth)

                   // if (Math.abs(diff) <= 30f) {
                        createMarkerView(marker, distance.toDouble(), diff)
                    //}
                }
            }
        }
    }

    private fun createMarkerView(marker: Marker, distance: Double, azimuthDiff: Float) {
        val view = MarkerView(context, marker, distance)

        val screenWidth = container.width
        val centerX = screenWidth / 2f
        val offset = (azimuthDiff / 60f) * centerX
        val x = centerX + offset

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = (x - 50).toInt()
        params.topMargin = 300

        view.layoutParams = params
        container.addView(view)
        markerViews[marker.id] = view

        view.setOnClickListener {
            val intent = Intent(context, ReviewActivity::class.java)
            intent.putExtra("marker_id", marker.id)
            context.startActivity(intent)
        }
    }

    fun onDestroy() {
        executor.shutdown()
    }
}
