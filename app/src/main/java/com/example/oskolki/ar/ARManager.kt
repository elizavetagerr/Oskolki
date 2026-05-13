package com.example.oskolki.ar

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.example.oskolki.ReviewActivity
import com.example.oskolki.model.Marker
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ARManager(
    private val context: Context,
    private val container: FrameLayout
) {
    private val markers = mutableListOf<Marker>()
    private val markerViews = mutableMapOf<String, MarkerView>()
    private val foundMarkerIds = mutableSetOf<String>()
    private var currentAzimuth: Float = 0f
    private var currentLocation: android.location.Location? = null
    private var isDestroyed = false

    fun loadMarkers(markers: List<Marker>) {
        this.markers.clear()
        this.markers.addAll(markers)
    }

    fun updateMarkers(location: android.location.Location, azimuth: Float) {
        if (isDestroyed) return
        currentLocation = location
        currentAzimuth = azimuth
        updateMarkerViews()
    }

    private fun updateMarkerViews() {
        val loc = currentLocation
        val azi = currentAzimuth
        Log.d("ARManager", "updateMarkerViews loc=$loc, azimuth=$azi, markersCount=${markers.size}, viewsCount=${markerViews.size}")

        if (loc == null) {
            Log.w("ARManager", "Location is null, skipping update")
            return
        }

        val toRemove = markerViews.keys.toMutableSet()

        markers.forEach { marker ->
            val distance = MathUtils.calculateDistance(loc, marker)
            Log.d("ARManager", "Marker ${marker.id}: dist=${distance}, azimDiff=${MathUtils.calculateAzimuthDifference(MathUtils.calculateAzimuth(loc, marker), azi)}")

            if (distance <= 500f) {
                toRemove.remove(marker.id)

                val markerAzimuth = MathUtils.calculateAzimuth(loc, marker)
                val diff = MathUtils.calculateAzimuthDifference(markerAzimuth, azi)

                val existing = markerViews[marker.id]
                if (existing != null) {
                    updateMarkerPosition(existing, diff)
                } else {
                    createMarkerView(marker, distance.toDouble(), diff)
                }
            }
        }

        toRemove.forEach { id ->
            markerViews[id]?.let {
                container.removeView(it)
                markerViews.remove(id)
            }
        }
    }

    private fun updateMarkerPosition(view: MarkerView, azimuthDiff: Float) {
        val screenWidth = container.width
        val centerX = screenWidth / 2f
        val offset = (azimuthDiff / 60f) * centerX
        val x = centerX + offset

        (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
            leftMargin = (x - 50).toInt()
            view.layoutParams = this
        }
    }

    private fun createMarkerView(marker: Marker, distance: Double, azimuthDiff: Float) {
        val view = MarkerView(context, marker, distance)

        updateMarkerPosition(view, azimuthDiff)
        if (view.layoutParams == null) {
            view.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        (view.layoutParams as FrameLayout.LayoutParams).topMargin = container.height / 3;

        container.addView(view)
        markerViews[marker.id] = view

        view.setOnClickListener {
            if (foundMarkerIds.contains(marker.id)) return@setOnClickListener

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.markFragmentFound(marker.id)
                    }
                    foundMarkerIds.add(marker.id)
                    Toast.makeText(context, "Осколок найден!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, ReviewActivity::class.java)
                    intent.putExtra("fragment_id", marker.id)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("ARManager", "Error marking fragment as found", e)
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onDestroy() {
        isDestroyed = true
        container.post {
            markerViews.values.forEach {
                container.removeView(it)
            }
            markerViews.clear()
        }
        markers.clear()
    }
}
