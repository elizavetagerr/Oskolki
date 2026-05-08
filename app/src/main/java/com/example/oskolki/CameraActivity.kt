package com.example.oskolki

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.oskolki.ar.ARManager
import com.example.oskolki.ar.LocationHelper
import com.example.oskolki.ar.SensorHelper
import com.example.oskolki.model.Marker
import com.example.oskolki.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var arManager: ARManager
    private lateinit var locationHelper: LocationHelper
    private lateinit var sensorHelper: SensorHelper
    private lateinit var bottomNavigation: BottomNavigationView

    private var markers = listOf<Marker>()
    private var lastLocation: android.location.Location? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        val container = findViewById<android.widget.FrameLayout>(R.id.ar_container)
        arManager = ARManager(this, container)
        locationHelper = LocationHelper(this)
        sensorHelper = SensorHelper(this)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupBottomNavigation()

        if (allPermissionsGranted()) {
            startCamera()
            startAR()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_cam

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> {
                    startActivity(Intent(this, FoundOskActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_edit -> {
                    startActivity(Intent(this, EditOskolokActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_cam -> {
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture?.addListener({
            try {
                val cameraProvider = cameraProviderFuture?.get()
                if (cameraProvider != null) {
                    bindPreview(cameraProvider)
                }
            } catch (e: Exception) {
                Log.e("CameraActivity", "Error: ${e.message}")
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        try {
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            Log.d("CameraActivity", "Preview bound successfully")
        } catch (e: Exception) {
            Log.e("CameraActivity", "Error: ${e.message}")
            Toast.makeText(this, "Ошибка предпросмотра: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAR() {
        loadMarkersFromApi()

        locationHelper.startLocationUpdates { location ->
            handleLocationUpdate(location)
        }

        sensorHelper.startSensorUpdates { orientation ->
            handleOrientationUpdate(orientation)
        }
    }

    private fun loadMarkersFromApi() {
        lifecycleScope.launch {
            try {
                val currentLocation = locationHelper.getCurrentLocation()
                if (currentLocation != null) {
                    markers = RetrofitClient.apiService.getFragments(
                        lat = currentLocation.latitude,
                        lng = currentLocation.longitude,
                        radius = 50
                    )
                    arManager.loadMarkers(markers)
                    Log.d("CameraActivity", "Loaded ${markers.size} markers")
                } else {
                    Log.w("CameraActivity", "Current location is null")
                }
            } catch (e: Exception) {
                Log.e("CameraActivity", "Error loading markers", e)
                Toast.makeText(this@CameraActivity, "Ошибка загрузки меток", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLocationUpdate(location: android.location.Location) {
        if (lastLocation == null || calculateDistance(lastLocation!!, location) > 10.0) {
            lastLocation = location
            loadMarkersFromApi()
            updateMarkers()
        }
    }

    private fun handleOrientationUpdate(orientation: FloatArray) {
        updateMarkers()
    }

    private fun updateMarkers() {
        val currentLocation = locationHelper.getCurrentLocation() ?: return
        val currentOrientation = sensorHelper.getCurrentOrientation() ?: return

        arManager.updateMarkers(currentLocation, currentOrientation[0])
    }

    private fun calculateDistance(loc1: android.location.Location, loc2: android.location.Location): Double {
        return loc1.distanceTo(loc2).toDouble()
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
                startAR()
            } else {
                Toast.makeText(this, "Разрешения необходимы", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
        sensorHelper.stopSensorUpdates()
    }
}
