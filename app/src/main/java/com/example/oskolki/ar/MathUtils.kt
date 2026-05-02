package com.example.oskolki.ar

import android.location.Location
import com.example.oskolki.model.Marker
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MathUtils {
    fun calculateDistance(userLocation: Location, marker: Marker): Float {
        val userLat = Math.toRadians(userLocation.latitude)
        val userLon = Math.toRadians(userLocation.longitude)
        val markerLat = Math.toRadians(marker.latitude)
        val markerLon = Math.toRadians(marker.longitude)

        val dLat = markerLat - userLat
        val dLon = markerLon - userLon

        val a = sin(dLat / 2).pow(2) +
                cos(userLat) * cos(markerLat) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (6371000 * c).toFloat()
    }

    fun calculateAzimuth(userLocation: Location, marker: Marker): Float {
        val userLat = Math.toRadians(userLocation.latitude)
        val userLon = Math.toRadians(userLocation.longitude)
        val markerLat = Math.toRadians(marker.latitude)
        val markerLon = Math.toRadians(marker.longitude)

        val dLon = markerLon - userLon

        val x = sin(dLon) * cos(markerLat)
        val y = cos(userLat) * sin(markerLat) -
                sin(userLat) * cos(markerLat) * cos(dLon)

        var azimuth = Math.toDegrees(atan2(x, y)).toFloat()

        azimuth = (azimuth + 360f) % 360f

        return azimuth
    }

    fun isMarkerVisible(markerAzimuth: Float, deviceOrientation: FloatArray): Boolean {
        val deviceAzimuth = Math.toDegrees(deviceOrientation[0].toDouble()).toFloat()
        val fov = 60f

        var diff = abs(markerAzimuth - deviceAzimuth)
        if (diff > 180) diff = 360f - diff

        return diff <= fov / 2
    }

    fun calculateAzimuthDifference(markerAzimuth: Float, currentAzimuth: Float): Float {
        var diff = markerAzimuth - currentAzimuth
        while (diff > 180) diff -= 360f
        while (diff < -180) diff += 360f
        return diff
    }
}
