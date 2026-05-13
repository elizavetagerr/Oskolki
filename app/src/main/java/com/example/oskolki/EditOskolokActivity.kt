package com.example.oskolki

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.oskolki.network.RetrofitClient
import com.example.oskolki.ar.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditOskolokActivity : AppCompatActivity() {
    private lateinit var etComment: EditText
    private lateinit var tvAddress: TextView
    private lateinit var btnAddMedia: Button
    private lateinit var btnAddAudio: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnClose: ImageButton
    
    private var currentLat: Double = 55.7558
    private var currentLng: Double = 37.6173
    
    private val selectedPhotoUris = mutableListOf<Uri>()
    private var selectedAudioUri: Uri? = null
    private lateinit var locationHelper: LocationHelper
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedPhotoUris.clear()
        selectedPhotoUris.addAll(uris)
        btnAddMedia.text = if (uris.size > 1) "Фото (${uris.size})" else if (uris.size == 1) "Фото выбрано" else "Добавить фото"
    }
    
    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedAudioUri = it
            btnAddAudio.text = "Аудио выбрано"
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Требуется разрешение на геолокацию", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_oskolok)
        
        initViews()
        setupListeners()
        checkLocationPermission()
    }
    
    private fun initViews() {
        etComment = findViewById(R.id.et_comment)
        tvAddress = findViewById(R.id.tv_address)
        btnAddMedia = findViewById(R.id.btn_add_media)
        btnAddAudio = findViewById(R.id.btn_add_audio)
        btnSubmit = findViewById(R.id.btn_submit)
        btnClose = findViewById(R.id.btn_close)
        
        locationHelper = LocationHelper(this)
    }
    
    private fun setupListeners() {
        btnAddMedia.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        
        btnAddAudio.setOnClickListener {
            pickAudioLauncher.launch("audio/*")
        }
        
        btnSubmit.setOnClickListener {
            submitFragment()
        }
        
        btnClose.setOnClickListener {
            finish()
        }
        
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_edit
        
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> {
                    startActivity(Intent(this, FoundOskActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_edit -> {
                    true
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
    
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            getCurrentLocation()
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationHelper.getCurrentLocation { location ->
            location?.let {
                currentLat = it.latitude
                currentLng = it.longitude
                reverseGeocode(it.latitude, it.longitude)
            }
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@EditOskolokActivity, java.util.Locale("ru"))
                val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
                withContext(Dispatchers.Main) {
                    val addressText = addresses?.firstOrNull()?.let { addr ->
                        listOfNotNull(
                            addr.thoroughfare,
                            addr.subThoroughfare
                        ).takeIf { it.isNotEmpty() }?.joinToString(", ")
                            ?: addr.locality
                    }
                    tvAddress.text = addressText ?: "%.5f, %.5f".format(lat, lng)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvAddress.text = "%.5f, %.5f".format(lat, lng)
                }
            }
        }
    }
    
    private fun submitFragment() {
        val text = etComment.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Введите текст осколка", Toast.LENGTH_SHORT).show()
            return
        }
        
        btnSubmit.isEnabled = false
        btnSubmit.text = "Отправка..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val textBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
                val latBody = currentLat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lngBody = currentLng.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                val photos = if (selectedPhotoUris.isNotEmpty()) {
                    selectedPhotoUris.mapNotNull { uri ->
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            val file = File(cacheDir, "photo_${System.currentTimeMillis()}_${uri.hashCode()}.jpg")
                            file.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("photos", file.name, requestBody)
                        }
                    }.ifEmpty { null }
                } else null
                
                val sound = selectedAudioUri?.let { uri ->
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val file = File(cacheDir, "audio_${System.currentTimeMillis()}.mp3")
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("sound", file.name, requestBody)
                    }
                }
                
                val response = RetrofitClient.apiService.createFragment(textBody, latBody, lngBody, photos, sound)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditOskolokActivity, "Осколок успешно создан!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditOskolokActivity, FoundOskActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Отправить"
                    Toast.makeText(this@EditOskolokActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}