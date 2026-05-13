package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.oskolki.model.MarkerDetail
import com.example.oskolki.model.ProfileResponse
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoundOskActivity : AppCompatActivity() {

    private lateinit var containerOsk: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_found_osk)

        containerOsk = findViewById(R.id.container_osk)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupBottomNavigation()
        loadFoundFragments()
    }

    private fun loadFoundFragments() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val ids = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getFoundFragmentIds()
                }

                val details = withContext(Dispatchers.IO) {
                    coroutineScope {
                        ids.ids.map { id ->
                            async { RetrofitClient.apiService.getFragmentDetail(id) }
                        }.map { it.await() }
                    }
                }

                val userIds = details.mapNotNull { it.userId }.distinct()
                val userCache = mutableMapOf<String, ProfileResponse>()
                userIds.forEach { uid ->
                    try {
                        userCache[uid] = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.getUser(uid)
                        }
                    } catch (_: Exception) {}
                }

                renderFragments(details, userCache)
            } catch (e: Exception) {
                Toast.makeText(this@FoundOskActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderFragments(fragments: List<MarkerDetail>, userCache: Map<String, ProfileResponse>) {
        containerOsk.removeAllViews()
        for (fragment in fragments) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_fragment_preview, containerOsk, false)

            val user = fragment.userId?.let { userCache[it] }
            val authorName = user?.name ?: "User"
            view.findViewById<TextView>(R.id.tv_author).text = authorName
            view.findViewById<TextView>(R.id.tv_preview).text = fragment.text.take(80)
            view.findViewById<TextView>(R.id.tv_date).text = fragment.createdAt.take(10)

            val avatarUrl = user?.avatarUrl
            if (avatarUrl != null) {
                val fullUrl = if (avatarUrl.startsWith("/")) BuildConfig.BASE_URL.trimEnd('/') + avatarUrl else avatarUrl
                Glide.with(this)
                    .load(fullUrl)
                    .circleCrop()
                    .into(view.findViewById(R.id.iv_author_avatar))
            }

            view.setOnClickListener {
                val intent = Intent(this, ReviewActivity::class.java)
                intent.putExtra("fragment_id", fragment.id)
                startActivity(intent)
            }
            containerOsk.addView(view)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_osk

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> true
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
