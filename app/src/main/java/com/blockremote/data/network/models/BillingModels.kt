package com.blockremote.data.network.models

import com.google.gson.annotations.SerializedName

data class BillingStatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("plan") val plan: String,
    @SerializedName("trial_expired") val trialExpired: Boolean,
    @SerializedName("trial_days_remaining") val trialDaysRemaining: Int,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("device_id") val deviceId: String?
)

data class SignalPayload(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("accessibility_active") val accessibilityActive: Boolean,
    @SerializedName("window_mirroring") val windowMirroring: Boolean,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("threat_level") val threatLevel: Float,
    @SerializedName("sensors_active") val sensorsActive: Int
)

data class ServerCommand(
    @SerializedName("action") val action: String,
    @SerializedName("payload") val payload: Map<String, Any>? = null,
    @SerializedName("signature") val signature: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0
)

data class AuthTokenRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("os_version") val osVersion: String
)

data class AuthTokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("device_id") val deviceId: String
)
