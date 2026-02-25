package com.blockremote.data.local

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.util.UUID

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val deviceId: String by lazy {
        val stored = prefs.getString(KEY_DEVICE_ID, null)
        if (stored != null) {
            stored
        } else {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: UUID.randomUUID().toString()
            val id = "BR-$androidId"
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            id
        }
    }

    fun getDeviceId(): String = deviceId

    fun getJwtToken(): String? = prefs.getString(KEY_JWT_TOKEN, null)

    fun saveJwtToken(token: String, expiresIn: Long) {
        prefs.edit()
            .putString(KEY_JWT_TOKEN, token)
            .putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresIn * 1000))
            .apply()
    }

    fun isTokenValid(): Boolean {
        val token = getJwtToken() ?: return false
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return token.isNotBlank() && System.currentTimeMillis() < expiry
    }

    fun getTrialStartTimestamp(): Long {
        val ts = prefs.getLong(KEY_TRIAL_START, 0)
        if (ts == 0L) {
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_TRIAL_START, now).apply()
            return now
        }
        return ts
    }

    fun isLocalTrialExpired(): Boolean {
        val start = getTrialStartTimestamp()
        val elapsed = System.currentTimeMillis() - start
        return elapsed > TRIAL_DURATION_MS
    }

    fun getTrialDaysRemaining(): Int {
        val start = getTrialStartTimestamp()
        val elapsed = System.currentTimeMillis() - start
        val remaining = TRIAL_DURATION_MS - elapsed
        return if (remaining > 0) (remaining / DAY_MS).toInt() else 0
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "blockremote_sentinel"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_TRIAL_START = "trial_start_ts"

        private const val DAY_MS = 86_400_000L
        private const val TRIAL_DURATION_MS = 7 * DAY_MS
    }
}
