package com.blockremote.domain.usecases

import com.blockremote.data.sensors.RawSensorData
import kotlin.math.abs
import kotlin.math.sqrt

data class ThreatAnalysis(
    val isThreat: Boolean,
    val confidence: Float,
    val type: ThreatType,
    val description: String
)

enum class ThreatType {
    NONE,
    SENSOR_ANOMALY,
    ACCESSIBILITY_INJECTION,
    REMOTE_ACCESS,
    NETWORK_INTRUSION
}

class DetectThreatUseCase {

    private var baselineAccel: FloatArray = floatArrayOf(0f, 0f, 9.81f)
    private var isCalibrated = false
    private val recentMagnitudes = mutableListOf<Float>()

    fun analyzeAccelerometerData(
        data: RawSensorData,
        sensitivity: Float = 0.5f
    ): ThreatAnalysis {
        if (!isCalibrated) {
            baselineAccel = data.values.copyOf()
            isCalibrated = true
            return ThreatAnalysis(false, 0f, ThreatType.NONE, "Calibrating sensors...")
        }

        val dx = abs(data.values[0] - baselineAccel[0])
        val dy = abs(data.values[1] - baselineAccel[1])
        val dz = abs(data.values[2] - baselineAccel[2])
        val magnitude = sqrt(dx * dx + dy * dy + dz * dz)

        recentMagnitudes.add(magnitude)
        if (recentMagnitudes.size > 50) recentMagnitudes.removeAt(0)

        val threshold = 3.0f * (1.0f - sensitivity * 0.8f)
        val avgMagnitude = recentMagnitudes.average().toFloat()

        return if (avgMagnitude > threshold) {
            ThreatAnalysis(
                isThreat = true,
                confidence = (avgMagnitude / (threshold * 2)).coerceIn(0f, 1f),
                type = ThreatType.SENSOR_ANOMALY,
                description = "Anomalous device motion detected: ${String.format("%.2f", avgMagnitude)}g deviation"
            )
        } else {
            ThreatAnalysis(
                isThreat = false,
                confidence = 0f,
                type = ThreatType.NONE,
                description = "Sensor telemetry nominal"
            )
        }
    }

    fun analyzeAccessibilityServices(
        activeServices: List<String>,
        threshold: Float = 0.7f
    ): ThreatAnalysis {
        val suspiciousPatterns = listOf(
            "remote", "control", "access", "vnc", "rdp",
            "mirror", "cast", "spy", "monitor", "inject"
        )

        val suspiciousServices = activeServices.filter { service ->
            suspiciousPatterns.any { pattern ->
                service.lowercase().contains(pattern)
            }
        }

        val riskScore = suspiciousServices.size.toFloat() / activeServices.size.coerceAtLeast(1)

        return if (riskScore >= threshold) {
            ThreatAnalysis(
                isThreat = true,
                confidence = riskScore,
                type = ThreatType.ACCESSIBILITY_INJECTION,
                description = "Suspicious accessibility services detected: ${suspiciousServices.joinToString()}"
            )
        } else {
            ThreatAnalysis(
                isThreat = false,
                confidence = riskScore,
                type = ThreatType.NONE,
                description = "${activeServices.size} accessibility services monitored"
            )
        }
    }

    fun reset() {
        isCalibrated = false
        recentMagnitudes.clear()
    }
}
