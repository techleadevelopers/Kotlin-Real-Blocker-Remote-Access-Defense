package com.blockremote.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class RawSensorData(
    val type: Int,
    val values: FloatArray,
    val accuracy: Int,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawSensorData) return false
        return type == other.type && values.contentEquals(other.values) &&
                accuracy == other.accuracy && timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + values.contentHashCode()
        result = 31 * result + accuracy
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

class SensorRepository(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun observeAccelerometer(): Flow<RawSensorData> = observeSensor(Sensor.TYPE_ACCELEROMETER)

    fun observeGyroscope(): Flow<RawSensorData> = observeSensor(Sensor.TYPE_GYROSCOPE)

    fun observeMagnetometer(): Flow<RawSensorData> = observeSensor(Sensor.TYPE_MAGNETIC_FIELD)

    fun getAvailableSensors(): List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

    private fun observeSensor(sensorType: Int): Flow<RawSensorData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(sensorType)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    RawSensorData(
                        type = event.sensor.type,
                        values = event.values.copyOf(),
                        accuracy = event.accuracy,
                        timestamp = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
