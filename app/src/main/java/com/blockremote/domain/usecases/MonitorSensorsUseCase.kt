package com.blockremote.domain.usecases

import com.blockremote.data.sensors.RawSensorData
import com.blockremote.data.sensors.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SensorSnapshot(
    val accelerometer: RawSensorData?,
    val gyroscope: RawSensorData?,
    val magnetometer: RawSensorData?,
    val timestamp: Long = System.currentTimeMillis()
)

class MonitorSensorsUseCase(
    private val repository: SensorRepository
) {
    fun observeAllSensors(): Flow<SensorSnapshot> {
        return combine(
            repository.observeAccelerometer(),
            repository.observeGyroscope(),
            repository.observeMagnetometer()
        ) { accel, gyro, mag ->
            SensorSnapshot(
                accelerometer = accel,
                gyroscope = gyro,
                magnetometer = mag
            )
        }
    }

    fun observeAccelerometer(): Flow<RawSensorData> = repository.observeAccelerometer()

    fun observeGyroscope(): Flow<RawSensorData> = repository.observeGyroscope()

    fun getAvailableSensorCount(): Int = repository.getAvailableSensors().size
}
