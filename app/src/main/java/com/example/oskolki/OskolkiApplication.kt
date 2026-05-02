package com.example.oskolki

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class OskolkiApplication : Application() {
    companion object {
        lateinit var instance: OskolkiApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Инициализация Яндекс Карт при запуске приложения (один раз)
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
        MapKitFactory.initialize(this)
    }
}