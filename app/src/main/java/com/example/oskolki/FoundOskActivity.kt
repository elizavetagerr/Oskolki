package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class FoundOskActivity : AppCompatActivity() {

    private lateinit var containerOsk: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_found_osk)

        // Инициализация View
        containerOsk = findViewById(R.id.container_osk)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Обработчик нажатия на контейнер осколка
        containerOsk.setOnClickListener {
            // Переход на экран ReviewActivity
            val intent = Intent(this, ReviewActivity::class.java)
            // Если нужно передать ID осколка:
            // intent.putExtra("marker_id", "id_осколка")
            startActivity(intent)
        }

        // Настройка нижней навигации
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_osk

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> {
                    // Уже на этом экране
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}