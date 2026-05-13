package com.example.oskolki.ar

import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import android.opengl.Matrix
import com.example.oskolki.model.Marker
import kotlin.math.*

object MathUtils {

    /**
     * Правильное получение азимута устройства для AR
     * с учётом того что телефон держат ВЕРТИКАЛЬНО
     *
     * @param rotationMatrix - матрица из SensorManager.getRotationMatrix()
     * @param userLocation - для расчёта магнитного склонения
     * @return азимут в градусах [0, 360)
     */
    fun getDeviceAzimuthForAR(
        rotationMatrix: FloatArray,
        userLocation: Location? = null
    ): Float {
        // Переопределяем систему координат для вертикального телефона
        // Когда телефон держат вертикально перед собой:
        // - ось X устройства = горизонталь экрана
        // - ось Y устройства = направление куда смотрит камера
        // - ось Z устройства = вверх по экрану
        val remappedMatrix = FloatArray(16)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,      // X остаётся X
            SensorManager.AXIS_Z,      // Z становится Y (направление взгляда)
            remappedMatrix
        )

        val orientation = FloatArray(3)
        SensorManager.getOrientation(remappedMatrix, orientation)

        var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()

        // Учитываем магнитное склонение если есть локация
        userLocation?.let {
            val geoField = GeomagneticField(
                it.latitude.toFloat(),
                it.longitude.toFloat(),
                it.altitude.toFloat(),
                System.currentTimeMillis()
            )
            azimuthDeg += geoField.declination
        }

        return (azimuthDeg + 360f) % 360f
    }

    /**
     * Расчёт истинного азимута от пользователя до маркера
     */
    fun calculateAzimuth(userLocation: Location, marker: Marker): Float {
        val userLat = Math.toRadians(userLocation.latitude)
        val userLon = Math.toRadians(userLocation.longitude)
        val markerLat = Math.toRadians(marker.latitude)
        val markerLon = Math.toRadians(marker.longitude)

        val dLon = markerLon - userLon

        val x = sin(dLon) * cos(markerLat)
        val y = cos(userLat) * sin(markerLat) -
                sin(userLat) * cos(markerLat) * cos(dLon)

        val azimuthRad = atan2(x, y)
        val azimuthDeg = Math.toDegrees(azimuthRad).toFloat()

        return (azimuthDeg + 360f) % 360f
    }

    /**
     * Расчёт расстояния (Haversine)
     */
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

    /**
     * Разница азимутов [-180, 180]
     * + = маркер правее, - = маркер левее
     */
    fun calculateAzimuthDifference(markerAzimuth: Float, deviceAzimuth: Float): Float {
        var diff = markerAzimuth - deviceAzimuth
        diff = ((diff + 180f) % 360f + 360f) % 360f - 180f
        return diff
    }

    /**
     * Попадает ли маркер в поле зрения
     */
    fun isMarkerVisible(
        markerAzimuth: Float,
        deviceAzimuth: Float,
        horizontalFov: Float = 60f
    ): Boolean {
        val diff = abs(calculateAzimuthDifference(markerAzimuth, deviceAzimuth))
        return diff <= horizontalFov / 2f
    }

    /**
     * Позиция маркера на экране по X
     */
    fun calculateScreenX(
        markerAzimuth: Float,
        deviceAzimuth: Float,
        screenWidth: Int,
        horizontalFov: Float = 60f
    ): Float {
        val diff = calculateAzimuthDifference(markerAzimuth, deviceAzimuth)
        val normalized = diff / (horizontalFov / 2f).coerceIn(-1f, 1f)
        return screenWidth / 2f + normalized * (screenWidth / 2f)
    }
}