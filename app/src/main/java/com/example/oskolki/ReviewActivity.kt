package com.example.oskolki

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.oskolki.model.MarkerDetail
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var playAudioButton: Button
    private lateinit var audioPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val markerId = intent.getStringExtra("marker_id")

        imageView = findViewById(R.id.iv_image)
        titleTextView = findViewById(R.id.tv_title)
        descriptionTextView = findViewById(R.id.tv_description)
        playAudioButton = findViewById(R.id.btn_play_audio)

        audioPlayer = MediaPlayer()

        playAudioButton.setOnClickListener {
            toggleAudio()
        }

        if (!markerId.isNullOrEmpty()) {
            loadMarkerDetail(markerId)
        } else {
            Toast.makeText(this, "Ошибка: ID метки не указан", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadMarkerDetail(markerId: String) {
        lifecycleScope.launch {
            try {
                val detail = RetrofitClient.apiService.getFragmentDetail(markerId)

                titleTextView.text = detail.text ?: "Без названия"
                descriptionTextView.text = "Создано: ${detail.createdAt}"

                val firstPhotoUrl = detail.photoUrls?.firstOrNull()
                if (firstPhotoUrl != null) {
                    Glide.with(this@ReviewActivity)
                        .load(firstPhotoUrl)
                        .into(imageView)
                }

                if (detail.audioUrl != null) {
                    loadAudio(detail.audioUrl)
                } else {
                    playAudioButton.isEnabled = false
                    playAudioButton.text = "Аудио недоступно"
                }

            } catch (e: Exception) {
                Toast.makeText(this@ReviewActivity, "Ошибка загрузки деталей", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAudio(audioUrl: String) {
        try {
            audioPlayer.reset()
            audioPlayer.setDataSource(audioUrl)
            audioPlayer.prepareAsync()
            audioPlayer.setOnPreparedListener {
                playAudioButton.text = "Воспроизвести аудио"
            }
        } catch (e: Exception) {
            playAudioButton.isEnabled = false
            playAudioButton.text = "Аудио недоступно"
        }
    }

    private fun toggleAudio() {
        if (audioPlayer.isPlaying) {
            audioPlayer.pause()
            playAudioButton.text = "Продолжить"
        } else {
            audioPlayer.start()
            playAudioButton.text = "Пауза"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}
