package com.example.oskolki.ar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorHelper(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensorListener: SensorEventListener? = null
    private var currentOrientation: FloatArray? = null

    fun startSensorUpdates(callback: (FloatArray) -> Unit) {
        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    currentOrientation = orientation
                    callback(orientation)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                sensorListener,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            Log.e("SensorHelper", "Rotation vector sensor not available")
        }
    }

    fun stopSensorUpdates() {
        sensorListener?.let {
            sensorManager.unregisterListener(it)
        }
    }

    fun getCurrentOrientation(): FloatArray? = currentOrientation
}
