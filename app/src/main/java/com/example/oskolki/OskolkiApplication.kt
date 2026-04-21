package com.example.oskolki

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class OskolkiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация Яндекс Карт при запуске приложения (один раз)
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
        MapKitFactory.initialize(this)
    }
}