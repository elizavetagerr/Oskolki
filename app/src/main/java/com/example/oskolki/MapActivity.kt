package com.example.oskolki

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.example.oskolki.model.MarkerDetail
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.yandex.runtime.image.ImageProvider
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapview)
        progressBar = findViewById(R.id.progress_bar)

        val map = mapView.mapWindow.map

        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        mapObjects = map.mapObjects.addCollection()

        setupBottomNavigation()
        loadFoundFragments()
    }

    private fun loadFoundFragments() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val ids = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getFoundFragmentIds()
                }
                Log.d("MapActivity", "Found fragment IDs: ${ids.ids}")

                val details = withContext(Dispatchers.IO) {
                    coroutineScope {
                        ids.ids.map { id ->
                            async { RetrofitClient.apiService.getFragmentDetail(id) }
                        }.map { it.await() }
                    }
                }
                Log.d("MapActivity", "Loaded ${details.size} fragments:")
                details.forEach { f ->
                    Log.d("MapActivity", "  → id=${f.id}, lat=${f.latitude}, lng=${f.longitude}, text=${f.text?.take(30)}")
                }

                val icons = withContext(Dispatchers.IO) {
                    buildMarkerIcons(details)
                }
                renderMarkers(icons)
                if (details.isNotEmpty()) {
                    centerOnFirst(details.first())
                } else {
                    Log.w("MapActivity", "No fragments to render — list is empty")
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error loading fragments", e)
                Toast.makeText(this@MapActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun renderMarkers(icons: List<Pair<MarkerDetail, ImageProvider>>) {
        Log.d("MapActivity", "renderMarkers: clearing existing objects")
        mapObjects.clear()

        Log.d("MapActivity", "renderMarkers: adding ${icons.size} placemarks")
        for ((fragment, icon) in icons) {
            val point = Point(fragment.latitude, fragment.longitude)
            Log.d("MapActivity", "  creating placemark at ($point)")
            val mapMarker = mapObjects.addPlacemark(point, icon)
            mapMarker.userData = fragment.id
            mapMarker.addTapListener { mapObject, _ ->
                val id = mapObject.userData as? String
                if (id != null) {
                    Log.d("MapActivity", "  tap on fragment $id")
                    val intent = Intent(this, ReviewActivity::class.java)
                    intent.putExtra("fragment_id", id)
                    startActivity(intent)
                }
                true
            }
        }
        Log.d("MapActivity", "renderMarkers: done, added ${icons.size} placemarks")
    }

    private fun buildMarkerIcons(fragments: List<MarkerDetail>): List<Pair<MarkerDetail, ImageProvider>> {
        return fragments.map { fragment ->
            val icon = if (!fragment.photoUrls.isNullOrEmpty()) {
                loadPhotoIcon(fragment.photoUrls.first())
            } else {
                createColoredDotIcon(fragment.expiresAt)
            }
            fragment to icon
        }
    }

    private fun loadPhotoIcon(url: String): ImageProvider {
        val fullUrl = if (url.startsWith("/")) BuildConfig.BASE_URL.trimEnd('/') + url else url
        Log.d("MapActivity", "loadPhotoIcon: original=$url -> full=$fullUrl")
        return try {
            val bitmap = Glide.with(applicationContext)
                .asBitmap()
                .load(fullUrl)
                .circleCrop()
                .submit(64, 64)
                .get(5, TimeUnit.SECONDS)
            Log.d("MapActivity", "loadPhotoIcon: bitmap loaded successfully, size=${bitmap.width}x${bitmap.height}")
            ImageProvider.fromBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("MapActivity", "loadPhotoIcon: failed to load $fullUrl", e)
            createColoredDotIcon(null)
        }
    }

    private fun createColoredDotIcon(expiresAt: String?): ImageProvider {
        val size = 48
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val color = timeBasedColor(expiresAt)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, fillPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 1.5f, strokePaint)

        return ImageProvider.fromBitmap(bitmap)
    }

    private fun timeBasedColor(expiresAt: String?): Int {
        if (expiresAt == null) return Color.rgb(0, 200, 0)

        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val expires = fmt.parse(expiresAt)?.time ?: return Color.rgb(0, 200, 0)
            val remaining = expires - System.currentTimeMillis()
            val maxTime = 30L * 24 * 60 * 60 * 1000
            val ratio = (remaining.toFloat() / maxTime).coerceIn(0f, 1f)

            val h = 120f * ratio
            val s = 0.4f + 0.6f * ratio
            val v = 0.4f + 0.6f * ratio
            val rgb = Color.HSVToColor(floatArrayOf(h, s, v))
            Color.rgb(Color.red(rgb), Color.green(rgb), Color.blue(rgb))
        } catch (_: Exception) {
            Color.rgb(0, 200, 0)
        }
    }

    private fun centerOnFirst(fragment: com.example.oskolki.model.MarkerDetail) {
        val target = Point(fragment.latitude, fragment.longitude)
        Log.d("MapActivity", "centerOnFirst: moving camera to $target zoom=10")
        mapView.mapWindow.map.move(
            com.yandex.mapkit.map.CameraPosition(target, 10.0f, 0.0f, 0.0f)
        )
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.nav_map
            setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_osk -> {
                        startActivity(Intent(this@MapActivity, FoundOskActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_edit -> {
                        startActivity(Intent(this@MapActivity, EditOskolokActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_map -> true
                    R.id.nav_cam -> {
                        startActivity(Intent(this@MapActivity, CameraActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_profile -> {
                        startActivity(Intent(this@MapActivity, ProfileActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }
}
