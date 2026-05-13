package com.example.oskolki

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.example.oskolki.network.RetrofitClient
import com.yandex.mapkit.MapKitFactory
import java.io.InputStream

class OskolkiApplication : Application() {
    companion object {
        lateinit var instance: OskolkiApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Glide.get(this).registry.replace(
            GlideUrl::class.java, InputStream::class.java,
            OkHttpUrlLoader.Factory(RetrofitClient.httpClient)
        )

        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
        MapKitFactory.initialize(this)
    }
}