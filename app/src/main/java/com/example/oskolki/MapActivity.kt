package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import android.widget.Toast

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_map

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> {
                    Toast.makeText(this, "Раздел в разработке", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_edit -> {
                    startActivity(Intent(this, EditOskolokActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_map -> {
                    true
                }
                R.id.nav_cam -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Раздел в разработке", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        val mapView = findViewById<MapView>(R.id.mapview)
        val userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        val targetPoint = Point(59.9311, 30.3609)
        mapView.map.move(
            com.yandex.mapkit.map.CameraPosition(targetPoint, 10.0f, 0.0f, 0.0f)
        )
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