package com.example.oskolki

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast

class EditOskolokActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_oskolok)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Устанавливаем активную иконку - Создать
        bottomNavigation.selectedItemId = R.id.nav_edit

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> {
                    startActivity(Intent(this, FoundOskActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_edit -> {
                    true  // уже на этом экране
                }
                R.id.nav_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_cam -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    finish()
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
}