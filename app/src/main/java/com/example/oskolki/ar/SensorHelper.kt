package com.example.oskolki.ar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import android.view.WindowManager

class SensorHelper(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensorListener: SensorEventListener? = null
    private var currentOrientation: Float? = null

    fun startSensorUpdates(callback: (Float) -> Unit) {
        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
                    event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
                ) {
                    val rotationMatrix = FloatArray(16)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val remappedMatrix = remapCoordinateSystem(rotationMatrix)

                    //val orientation = FloatArray(3)
                    val orientation = MathUtils.getDeviceAzimuthForAR(rotationMatrix)
                    //SensorManager.getOrientation(remappedMatrix, orientation)

                    currentOrientation = orientation
                    Log.d("SensorHelper", "Orientation: ${orientation}")
                    callback(orientation)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        var sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        if (sensor == null) {
            Log.d("SensorHelper", "GAME_ROTATION_VECTOR not available, falling back to ROTATION_VECTOR")
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        }
        if (sensor != null) {
            sensorManager.registerListener(
                sensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            Log.d("SensorHelper", "Registered ${sensor.stringType}")
        } else {
            Log.e("SensorHelper", "No rotation vector sensor available")
        }
    }

    private fun remapCoordinateSystem(rotationMatrix: FloatArray): FloatArray {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return rotationMatrix
        val displayRotation = windowManager.defaultDisplay.rotation

        val remapped = FloatArray(9)
        return when (displayRotation) {
            Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, remapped
                )
                remapped
            }
            Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, remapped
                )
                remapped
            }
            Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, remapped
                )
                remapped
            }
            else -> rotationMatrix
        }
    }

    fun stopSensorUpdates() {
        sensorListener?.let {
            sensorManager.unregisterListener(it)
        }
    }

    fun getCurrentOrientation(): Float? = currentOrientation
}
