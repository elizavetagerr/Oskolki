package com.example.oskolki

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oskolki.BuildConfig
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.oskolki.model.MarkerDetail
import com.example.oskolki.model.ProfileResponse
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvContentPart1: TextView
    private lateinit var llPhotos: LinearLayout
    private lateinit var hsvPhotos: HorizontalScrollView
    private lateinit var playerContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView

    private var exoPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var fragmentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        fragmentId = intent.getStringExtra("fragment_id")

        initViews()
        setupBottomNavigation()
        loadFragmentData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        seekBar = findViewById(R.id.seek_bar)
        tvTime = findViewById(R.id.tv_time)
        tvAuthorName = findViewById(R.id.tv_author_name)
        tvDate = findViewById(R.id.tv_date)
        tvLocation = findViewById(R.id.tv_location)
        tvContentPart1 = findViewById(R.id.tv_content_part1)
        llPhotos = findViewById(R.id.ll_photos)
        hsvPhotos = findViewById(R.id.hsv_photos)
        playerContainer = findViewById(R.id.player_container)
        progressBar = findViewById(R.id.progress_bar)
        scrollView = findViewById(R.id.sv_content)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_osk

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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragmentData() {
        val id = fragmentId
        if (id == null) {
            Toast.makeText(this, "ID фрагмента не указан", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val detail = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getFragmentDetail(id)
                }
                displayFragment(detail)
            } catch (e: Exception) {
                Toast.makeText(this@ReviewActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayFragment(detail: MarkerDetail) {
        progressBar.visibility = View.GONE
        scrollView.visibility = View.VISIBLE

        tvContentPart1.text = detail.text
        tvAuthorName.text = "…"
        tvDate.text = formatDate(detail.createdAt)
        tvLocation.text = "Загрузка..."

        loadAuthorName(detail.userId)

        if (detail.photoUrls.isNullOrEmpty()) {
            hsvPhotos.visibility = View.GONE
        } else {
            hsvPhotos.visibility = View.VISIBLE
            llPhotos.removeAllViews()
            val dp = resources.displayMetrics.density
            detail.photoUrls.forEachIndexed { index, photoUrl ->
                val fullUrl = if (photoUrl.startsWith("/")) BuildConfig.BASE_URL.trimEnd('/') + photoUrl else photoUrl
                Log.d("ReviewActivity", "Loading photo $index: original=$photoUrl -> full=$fullUrl")

                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        (200 * dp).toInt(),
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).also { if (index > 0) it.marginStart = (8 * dp).toInt() }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setBackgroundColor(0xFFF0F0F0.toInt())
                    setOnClickListener {
                        val intent = Intent(this@ReviewActivity, FullScreenImageActivity::class.java)
                        intent.putExtra("image_url", fullUrl)
                        startActivity(intent)
                    }
                }
                llPhotos.addView(imageView)

                Glide.with(this)
                    .load(fullUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView)
            }
        }

        if (detail.audioUrl != null) {
            playerContainer.visibility = View.VISIBLE
            val audioUrl = detail.audioUrl
            val fullAudioUrl = if (audioUrl.startsWith("/")) BuildConfig.BASE_URL.trimEnd('/') + audioUrl else audioUrl
            Log.d("ReviewActivity", "Loading audio: original=$audioUrl -> full=$fullAudioUrl")
            setupAudioPlayer(fullAudioUrl)
        } else {
            playerContainer.visibility = View.GONE
        }

        reverseGeocode(detail.latitude, detail.longitude)
    }

    private fun loadAuthorName(userId: String?) {
        if (userId == null) {
            tvAuthorName.text = "Пользователь"
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getUser(userId)
                }
                tvAuthorName.text = user.name ?: user.email
                if (user.avatarUrl != null) {
                    val fullUrl = if (user.avatarUrl.startsWith("/")) BuildConfig.BASE_URL.trimEnd('/') + user.avatarUrl else user.avatarUrl
                    Glide.with(this@ReviewActivity)
                        .load(fullUrl)
                        .circleCrop()
                        .into(findViewById(R.id.iv_avatar))
                }
            } catch (_: Exception) {
                tvAuthorName.text = "Пользователь"
            }
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@ReviewActivity, Locale("ru"))
                val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
                withContext(Dispatchers.Main) {
                    val addressText = addresses?.firstOrNull()?.let { addr ->
                        listOfNotNull(
                            addr.thoroughfare,
                            addr.subThoroughfare
                        ).takeIf { it.isNotEmpty() }?.joinToString(", ")
                            ?: addr.locality
                    }
                    tvLocation.text = addressText ?: "%.5f, %.5f".format(lat, lng)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvLocation.text = "%.5f, %.5f".format(lat, lng)
                }
            }
        }
    }

    private fun setupAudioPlayer(audioUrl: String) {
        val dataSourceFactory = OkHttpDataSource.Factory(RetrofitClient.httpClient)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(audioUrl))
        val player = ExoPlayer.Builder(this).build()
        exoPlayer = player

        player.setMediaSource(mediaSource)
        player.prepare()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val duration = player.duration.toInt()
                    seekBar.max = duration
                    tvTime.text = formatTime(duration)
                    updateSeekBar()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                btnPlayPause.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer?.seekTo(progress.toLong())
                    tvTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnPlayPause.setOnClickListener {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }
    }

    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition.toInt()
                        seekBar.progress = currentPosition
                        tvTime.text = formatTime(currentPosition)
                    }
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
        btnPlayPause.setImageResource(R.drawable.ic_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}