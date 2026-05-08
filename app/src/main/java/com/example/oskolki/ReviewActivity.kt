package com.example.oskolki

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ReviewActivity : AppCompatActivity() {

    // Элементы дизайна
    private lateinit var btnBack: ImageButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView

    // Аудио плеер
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // Инициализация View
        initViews()
        setupBottomNavigation()
        setupAudioPlayer()

        // Кнопка назад
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        seekBar = findViewById(R.id.seek_bar)
        tvTime = findViewById(R.id.tv_time)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_osk

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_osk -> {
                    startActivity(Intent(this, FoundOskActivity::class.java))
                    finish()
                    true}
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

    private fun setupAudioPlayer() {
        try {
            // Создаем MediaPlayer
            mediaPlayer = MediaPlayer()

            // Загружаем аудио из папки raw
            val afd = resources.openRawResourceFd(R.raw.audio_example)
            mediaPlayer?.apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
            }

            // Настраиваем SeekBar
            val duration = mediaPlayer?.duration ?: 0
            seekBar.max = duration
            tvTime.text = formatTime(duration)

            // Обновляем SeekBar во время воспроизведения
            updateSeekBar()

            // Обработка перемотки
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                        tvTime.text = formatTime(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = false
                }
            })

            // Кнопка Play/Pause
            btnPlayPause.setOnClickListener {
                togglePlayback()
            }

            // Когда аудио закончилось
            mediaPlayer?.setOnCompletionListener {
                btnPlayPause.setImageResource(R.drawable.ic_play)
                seekBar.progress = 0
                tvTime.text = formatTime(0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Если аудио нет, скрываем плеер
            findViewById<android.view.View>(R.id.player_container).visibility = android.view.View.GONE
        }
    }

    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                if (mediaPlayer?.isPlaying == true && !isUserSeeking) {
                    val currentPosition = mediaPlayer?.currentPosition ?: 0
                    seekBar.progress = currentPosition
                    tvTime.text = formatTime(currentPosition)
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun togglePlayback() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            btnPlayPause.setImageResource(R.drawable.ic_play)
        } else {
            mediaPlayer?.start()
            btnPlayPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        btnPlayPause.setImageResource(R.drawable.ic_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}