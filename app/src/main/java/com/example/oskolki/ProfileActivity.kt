package com.example.oskolki

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.example.oskolki.model.Achievement
import com.example.oskolki.model.ProfileResponse
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvUsername: TextView
    private lateinit var ivAvatar: ShapeableImageView
    private lateinit var btnLogout: ImageButton
    private lateinit var llAchievements: LinearLayout

    private var profile: ProfileResponse? = null

    private val avatarPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadAvatar(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        tvUsername = findViewById(R.id.tv_username)
        ivAvatar = findViewById(R.id.iv_avatar)
        btnLogout = findViewById(R.id.btn_logout)
        llAchievements = findViewById(R.id.ll_achievements)

        btnLogout.setOnClickListener {
            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "Выход выполнен", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        tvUsername.setOnClickListener { showEditNameDialog() }
        ivAvatar.setOnClickListener { avatarPickerLauncher.launch("image/*") }

        setupBottomNavigation()
        loadProfile()
        loadAchievements()
    }

    private fun loadProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                profile = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getProfile()
                }
                profile?.let { displayProfile(it) }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayProfile(p: ProfileResponse) {
        tvUsername.text = p.name ?: p.email
        if (p.avatarUrl != null) {
            val fullUrl = if (p.avatarUrl.startsWith("/")) BuildConfig.BASE_URL.trimEnd('/') + p.avatarUrl else p.avatarUrl
            Glide.with(this)
                .load(fullUrl)
                .circleCrop()
                .into(ivAvatar)
        }
    }

    private fun showEditNameDialog() {
        val current = profile?.name ?: profile?.email ?: ""
        val input = EditText(this).apply { setText(current) }
        AlertDialog.Builder(this)
            .setTitle("Изменить имя")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != current) {
                    updateProfileName(newName)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateProfileName(name: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val updated = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateProfile(nameBody, null)
                }
                profile = updated
                displayProfile(updated)
                Toast.makeText(this@ProfileActivity, "Имя сохранено", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadAvatar(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val avatarPart = withContext(Dispatchers.IO) {
                    val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                    contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("avatar", file.name, requestBody)
                }
                val nameBody = (profile?.name ?: profile?.email ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                val updated = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateProfile(nameBody, avatarPart)
                }
                profile = updated
                displayProfile(updated)
                Toast.makeText(this@ProfileActivity, "Аватар обновлён", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Ошибка загрузки аватара: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadAchievements() {
        CoroutineScope(Dispatchers.Main).launch {

            // Загружаем все ачивки
            var allAchievements: List<Achievement> = emptyList()
            try {
                val allDeferred = async(Dispatchers.IO) {
                    runCatching { RetrofitClient.apiService.getAchievements() }
                        .getOrNull() ?: emptyList()
                }
                allAchievements = allDeferred.await()
            } catch (e: Exception) {
                Log.e("Achievements", "Ошибка загрузки всех ачивок", e)
                displayAchievements(emptyList(), emptySet())
                return@launch // Нет смысла продолжать без общего списка
            }

            // Загружаем свои ачивки
            var unlockedIds: Set<String> = emptySet()
            try {
                val mineDeferred = async(Dispatchers.IO) {
                    runCatching { RetrofitClient.apiService.getMyAchievements() }
                        .getOrNull() ?: emptyList()
                }
                val myAchievements = mineDeferred.await()
                unlockedIds = myAchievements.map { it.id }.toSet()
            } catch (e: Exception) {
                Log.e("Achievements", "Ошибка загрузки своих ачивок", e)
                // unlockedIds остаётся emptySet() — показываем все как заблокированные
            }

            displayAchievements(allAchievements, unlockedIds)
        }
    }

    private fun displayAchievements(achievements: List<Achievement>, unlockedIds: Set<String>) {
        llAchievements.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (achievement in achievements) {
            val card = inflater.inflate(R.layout.item_achievement, llAchievements, false) as CardView
            card.findViewById<TextView>(R.id.tv_achievement_title).text = achievement.title
            card.findViewById<TextView>(R.id.tv_achievement_desc).text = achievement.description

            val check = card.findViewById<ImageView>(R.id.iv_achievement_check)
            if (achievement.id in unlockedIds) {
                check.alpha = 1f
                check.setColorFilter(0xFF22C55E.toInt())
            } else {
                check.alpha = 0.2f
                check.setColorFilter(0xFF9CA3AF.toInt())
            }

            llAchievements.addView(card)
        }
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
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
