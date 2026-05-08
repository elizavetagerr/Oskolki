package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ImageButton
class ProfileActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvUsername: TextView
    private lateinit var btnSettings: ImageButton  // ← ImageButton, не Button!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        tvUsername = findViewById(R.id.tv_username)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_profile

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
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cam -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    true  // уже на профиле
                }
                else -> false
            }
        }
    }
}