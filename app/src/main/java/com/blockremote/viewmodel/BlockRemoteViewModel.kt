package com.blockremote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blockremote.data.local.SessionManager
import com.blockremote.data.network.ApiClient
import com.blockremote.data.network.SentinelWebSocket
import com.blockremote.data.network.WebSocketState
import com.blockremote.data.network.models.ServerCommand
import com.blockremote.data.network.models.SignalPayload
import com.blockremote.data.repository.BillingRepository
import com.blockremote.data.repository.BillingResult
import com.blockremote.data.sensors.RawSensorData
import com.blockremote.domain.usecases.DetectThreatUseCase
import com.blockremote.domain.usecases.ThreatType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class SensorReading(
    val timestamp: Long = System.currentTimeMillis(),
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 9.81f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f
)

data class AuditLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO",
    val tag: String = "SYSTEM",
    val message: String = ""
)

data class AppPermission(
    val name: String,
    val packageName: String,
    val isAccessibilityService: Boolean = false,
    val isBlocked: Boolean = false,
    val riskLevel: String = "LOW"
)

enum class SystemLicenseState {
    LOADING,
    ACTIVE,
    TRIAL,
    PAYWALL,
    LOCKED,
    OFFLINE
}

data class BlockRemoteState(
    val isSystemSafe: Boolean = true,
    val isAlertMode: Boolean = false,
    val threatLevel: Float = 0f,
    val sensorReadings: List<SensorReading> = emptyList(),
    val auditLogs: List<AuditLogEntry> = emptyList(),
    val appPermissions: List<AppPermission> = emptyList(),
    val accessibilityThreshold: Float = 0.7f,
    val sensorSensitivity: Float = 0.5f,
    val motionDetectionEnabled: Boolean = true,
    val networkMonitorEnabled: Boolean = true,
    val accessibilityGuardEnabled: Boolean = true,
    val licenseState: SystemLicenseState = SystemLicenseState.LOADING,
    val trialDaysRemaining: Int = 7,
    val isSystemDeactivated: Boolean = false,
    val serverConfirmedSafe: Boolean = false,
    val webSocketState: WebSocketState = WebSocketState.DISCONNECTED,
    val showPaywall: Boolean = false
)

class BlockRemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(BlockRemoteState())
    val state: StateFlow<BlockRemoteState> = _state.asStateFlow()

    private val detectThreatUseCase = DetectThreatUseCase()
    private val sessionManager = SessionManager(application)
    private val billingRepository: BillingRepository
    private val sentinelWebSocket: SentinelWebSocket

    init {
        ApiClient.initialize(sessionManager)
        billingRepository = BillingRepository(sessionManager)
        sentinelWebSocket = SentinelWebSocket(ApiClient.createAuthenticatedOkHttpClient())

        initializeDefaultPermissions()
        addLogEntry("INFO", "BOOT", "BlockRemote Sentinel v2.0 initialized")
        addLogEntry("INFO", "DEVICE", "Device ID: ${sessionManager.getDeviceId()}")
        addLogEntry("INFO", "SENSOR", "Accelerometer calibration complete")
        addLogEntry("INFO", "GUARD", "Accessibility service monitor active")
        addLogEntry("INFO", "NETWORK", "Network traffic analyzer online")
        startSensorPolling()
    }

    fun checkBillingStatus() {
        viewModelScope.launch {
            addLogEntry("INFO", "BILLING", "Checking license status...")

            when (val result = billingRepository.checkBillingStatus()) {
                is BillingResult.Active -> {
                    _state.value = _state.value.copy(
                        licenseState = SystemLicenseState.ACTIVE,
                        isSystemDeactivated = false,
                        showPaywall = false
                    )
                    addLogEntry("INFO", "BILLING", "License active — plan: ${result.response.plan}")
                    connectWebSocket()
                }
                is BillingResult.Trial -> {
                    _state.value = _state.value.copy(
                        licenseState = SystemLicenseState.TRIAL,
                        trialDaysRemaining = result.daysRemaining,
                        isSystemDeactivated = false,
                        showPaywall = false
                    )
                    addLogEntry("WARN", "BILLING", "Trial mode — ${result.daysRemaining} days remaining")
                    connectWebSocket()
                }
                is BillingResult.PaywallRequired -> {
                    _state.value = _state.value.copy(
                        licenseState = SystemLicenseState.PAYWALL,
                        isSystemDeactivated = true,
                        showPaywall = true
                    )
                    addLogEntry("CRIT", "BILLING", "Trial expired — subscription required")
                }
                is BillingResult.Locked -> {
                    _state.value = _state.value.copy(
                        licenseState = SystemLicenseState.LOCKED,
                        isSystemDeactivated = true,
                        showPaywall = true
                    )
                    addLogEntry("CRIT", "BILLING", "System LOCKED by server command")
                }
                is BillingResult.Error -> {
                    val localExpired = sessionManager.isLocalTrialExpired()
                    _state.value = _state.value.copy(
                        licenseState = if (localExpired) SystemLicenseState.PAYWALL else SystemLicenseState.OFFLINE,
                        isSystemDeactivated = localExpired,
                        showPaywall = localExpired,
                        trialDaysRemaining = sessionManager.getTrialDaysRemaining()
                    )
                    addLogEntry("WARN", "BILLING", "Offline check — ${result.message}")
                    if (!localExpired) connectWebSocket()
                }
            }
        }
    }

    private fun connectWebSocket() {
        viewModelScope.launch {
            val url = ApiClient.getWebSocketUrl()
            val token = ApiClient.getJwtToken()

            addLogEntry("INFO", "WS", "Connecting to sentinel relay: $url")

            sentinelWebSocket.connect(url, token)
                .catch { e ->
                    addLogEntry("CRIT", "WS", "Connection failed: ${e.message}")
                    _state.value = _state.value.copy(webSocketState = WebSocketState.ERROR)
                }
                .collect { command ->
                    handleServerCommand(command)
                }
        }

        viewModelScope.launch {
            sentinelWebSocket.connectionState.collect { wsState ->
                _state.value = _state.value.copy(webSocketState = wsState)
            }
        }

        startHeartbeat()
    }

    private fun startHeartbeat() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)

                if (_state.value.webSocketState == WebSocketState.AUTHENTICATED ||
                    _state.value.webSocketState == WebSocketState.CONNECTED
                ) {
                    val payload = SignalPayload(
                        deviceId = sessionManager.getDeviceId(),
                        accessibilityActive = _state.value.accessibilityGuardEnabled,
                        windowMirroring = false,
                        timestamp = System.currentTimeMillis(),
                        threatLevel = _state.value.threatLevel,
                        sensorsActive = 3
                    )

                    val sent = sentinelWebSocket.sendHeartbeat(payload)
                    if (sent) {
                        addLogEntry("INFO", "HEARTBEAT", "Signal dispatched to sentinel relay")
                    } else {
                        addLogEntry("WARN", "HEARTBEAT", "Failed to dispatch — reconnecting")
                    }
                }
            }
        }
    }

    private fun handleServerCommand(command: ServerCommand) {
        when (command.action) {
            "CONNECTED" -> {
                addLogEntry("INFO", "WS", "Sentinel relay connected")
            }
            "AUTH_OK" -> {
                addLogEntry("INFO", "WS", "Zero-trust authentication confirmed")
            }
            "STATUS_SAFE" -> {
                _state.value = _state.value.copy(
                    serverConfirmedSafe = true,
                    isSystemSafe = true,
                    isAlertMode = false
                )
                addLogEntry("INFO", "SENTINEL", "Server confirmed: system SAFE")
            }
            "STATUS_AUDIT" -> {
                _state.value = _state.value.copy(
                    serverConfirmedSafe = false,
                    isSystemSafe = false
                )
                addLogEntry("WARN", "SENTINEL", "Server status: AUDIT MODE — awaiting confirmation")
            }
            "LOCK_SYSTEM" -> {
                addLogEntry("CRIT", "SENTINEL", "LOCK_SYSTEM command received — initiating shutdown")
                _state.value = _state.value.copy(
                    licenseState = SystemLicenseState.LOCKED,
                    isSystemDeactivated = true,
                    showPaywall = true
                )
                sessionManager.clearSession()
            }
            "FORCE_LOGOUT" -> {
                addLogEntry("CRIT", "SENTINEL", "FORCE_LOGOUT — clearing credentials")
                sessionManager.clearAll()
                _state.value = _state.value.copy(
                    licenseState = SystemLicenseState.LOCKED,
                    isSystemDeactivated = true,
                    showPaywall = true
                )
            }
            "BILLING_EXPIRED" -> {
                addLogEntry("CRIT", "BILLING", "Server reports subscription expired")
                _state.value = _state.value.copy(
                    licenseState = SystemLicenseState.PAYWALL,
                    isSystemDeactivated = true,
                    showPaywall = true
                )
            }
            "CONNECTION_ERROR" -> {
                val error = command.payload?.get("error") ?: "unknown"
                addLogEntry("WARN", "WS", "Connection error: $error")
            }
            else -> {
                addLogEntry("INFO", "WS", "Command received: ${command.action}")
            }
        }
    }

    fun dismissPaywall() {
        if (_state.value.licenseState != SystemLicenseState.LOCKED) {
            _state.value = _state.value.copy(showPaywall = false)
        }
    }

    fun simulateThreat() {
        _state.value = _state.value.copy(
            isSystemSafe = false,
            isAlertMode = true,
            threatLevel = 0.85f,
            serverConfirmedSafe = false
        )
        addLogEntry("CRIT", "THREAT", "Unauthorized remote access pattern detected")
        addLogEntry("WARN", "SENSOR", "Anomalous accelerometer spike: dx=4.2g")
        addLogEntry("CRIT", "A11Y", "Suspicious accessibility service injection: com.suspect.remote")
        addLogEntry("INFO", "SHIELD", "Countermeasures engaged — blocking threat vector")

        viewModelScope.launch {
            delay(5000)
            resetThreat()
        }
    }

    fun resetThreat() {
        detectThreatUseCase.reset()
        _state.value = _state.value.copy(
            isSystemSafe = true,
            isAlertMode = false,
            threatLevel = 0f
        )
        addLogEntry("INFO", "SYSTEM", "Threat neutralized — system restored to safe state")
    }

    fun togglePermission(index: Int) {
        val permissions = _state.value.appPermissions.toMutableList()
        if (index in permissions.indices) {
            permissions[index] = permissions[index].copy(
                isBlocked = !permissions[index].isBlocked
            )
            val perm = permissions[index]
            val action = if (perm.isBlocked) "BLOCKED" else "UNBLOCKED"
            addLogEntry("INFO", "SHIELD", "$action: ${perm.name} (${perm.packageName})")
            _state.value = _state.value.copy(appPermissions = permissions)
            runAccessibilityAnalysis()
        }
    }

    fun updateAccessibilityThreshold(value: Float) {
        _state.value = _state.value.copy(accessibilityThreshold = value)
        addLogEntry("INFO", "CONFIG", "Accessibility threshold set to ${(value * 100).toInt()}%")
    }

    fun updateSensorSensitivity(value: Float) {
        _state.value = _state.value.copy(sensorSensitivity = value)
        addLogEntry("INFO", "CONFIG", "Sensor sensitivity set to ${(value * 100).toInt()}%")
    }

    fun toggleMotionDetection(enabled: Boolean) {
        _state.value = _state.value.copy(motionDetectionEnabled = enabled)
        addLogEntry("INFO", "SENSOR", "Motion detection ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun toggleNetworkMonitor(enabled: Boolean) {
        _state.value = _state.value.copy(networkMonitorEnabled = enabled)
        addLogEntry("INFO", "NETWORK", "Network monitor ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun toggleAccessibilityGuard(enabled: Boolean) {
        _state.value = _state.value.copy(accessibilityGuardEnabled = enabled)
        addLogEntry("INFO", "GUARD", "Accessibility guard ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun processSensorEvent(sensorData: RawSensorData) {
        if (!_state.value.motionDetectionEnabled) return

        val analysis = detectThreatUseCase.analyzeAccelerometerData(
            data = sensorData,
            sensitivity = _state.value.sensorSensitivity
        )

        if (analysis.isThreat && analysis.type == ThreatType.SENSOR_ANOMALY) {
            _state.value = _state.value.copy(
                isSystemSafe = false,
                isAlertMode = true,
                threatLevel = analysis.confidence
            )
            addLogEntry("WARN", "SENSOR", analysis.description)
        }

        val reading = SensorReading(
            timestamp = sensorData.timestamp,
            accelerometerX = sensorData.values.getOrElse(0) { 0f },
            accelerometerY = sensorData.values.getOrElse(1) { 0f },
            accelerometerZ = sensorData.values.getOrElse(2) { 9.81f }
        )
        val readings = (_state.value.sensorReadings + reading).takeLast(100)
        _state.value = _state.value.copy(sensorReadings = readings)
    }

    private fun runAccessibilityAnalysis() {
        if (!_state.value.accessibilityGuardEnabled) return

        val activeServices = _state.value.appPermissions
            .filter { it.isAccessibilityService && !it.isBlocked }
            .map { it.packageName }

        val analysis = detectThreatUseCase.analyzeAccessibilityServices(
            activeServices = activeServices,
            threshold = _state.value.accessibilityThreshold
        )

        if (analysis.isThreat) {
            addLogEntry("WARN", "A11Y", analysis.description)
        }
    }

    fun addLogEntry(level: String, tag: String, message: String) {
        val entry = AuditLogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )
        val logs = (_state.value.auditLogs + entry).takeLast(200)
        _state.value = _state.value.copy(auditLogs = logs)
    }

    private fun initializeDefaultPermissions() {
        _state.value = _state.value.copy(
            appPermissions = listOf(
                AppPermission("TeamViewer", "com.teamviewer.teamviewer.market.mobile", true, false, "HIGH"),
                AppPermission("AnyDesk", "com.anydesk.anydeskandroid", true, false, "HIGH"),
                AppPermission("Chrome Remote", "com.google.chromeremotedesktop", true, false, "MEDIUM"),
                AppPermission("Accessibility Menu", "com.google.android.marvin.talkback", true, false, "LOW"),
                AppPermission("LastPass", "com.lastpass.lpandroid", true, false, "MEDIUM"),
                AppPermission("Tasker", "net.dinglisch.android.taskerm", true, false, "MEDIUM"),
                AppPermission("Unknown Service", "com.suspect.remote.access", true, true, "CRITICAL")
            )
        )
    }

    private fun startSensorPolling() {
        viewModelScope.launch {
            while (true) {
                delay(50)
                val noise = if (_state.value.isAlertMode) 4f else 0.3f
                val reading = SensorReading(
                    timestamp = System.currentTimeMillis(),
                    accelerometerX = (Math.random().toFloat() - 0.5f) * noise,
                    accelerometerY = (Math.random().toFloat() - 0.5f) * noise,
                    accelerometerZ = 9.81f + (Math.random().toFloat() - 0.5f) * noise,
                    gyroscopeX = (Math.random().toFloat() - 0.5f) * noise * 0.5f,
                    gyroscopeY = (Math.random().toFloat() - 0.5f) * noise * 0.5f,
                    gyroscopeZ = (Math.random().toFloat() - 0.5f) * noise * 0.5f
                )
                val readings = (_state.value.sensorReadings + reading).takeLast(100)
                _state.value = _state.value.copy(sensorReadings = readings)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sentinelWebSocket.disconnect()
    }
}
