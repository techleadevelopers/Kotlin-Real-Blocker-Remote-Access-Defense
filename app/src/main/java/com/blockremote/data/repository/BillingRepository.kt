package com.blockremote.data.repository

import com.blockremote.data.local.SessionManager
import com.blockremote.data.network.ApiClient
import com.blockremote.data.network.models.BillingStatusResponse

sealed class BillingResult {
    data class Active(val response: BillingStatusResponse) : BillingResult()
    data class Trial(val daysRemaining: Int) : BillingResult()
    object PaywallRequired : BillingResult()
    object Locked : BillingResult()
    data class Error(val message: String) : BillingResult()
}

class BillingRepository(
    private val sessionManager: SessionManager
) {
    suspend fun checkBillingStatus(): BillingResult {
        return try {
            val response = ApiClient.getApi().getBillingStatus()

            when (response.code()) {
                200 -> {
                    val body = response.body() ?: return BillingResult.Error("Empty response")

                    when {
                        body.isActive -> BillingResult.Active(body)
                        body.trialExpired -> BillingResult.PaywallRequired
                        body.trialDaysRemaining > 0 -> BillingResult.Trial(body.trialDaysRemaining)
                        else -> BillingResult.PaywallRequired
                    }
                }
                402 -> BillingResult.PaywallRequired
                403 -> BillingResult.Locked
                else -> BillingResult.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val localTrialExpired = sessionManager.isLocalTrialExpired()
            if (localTrialExpired) {
                BillingResult.PaywallRequired
            } else {
                BillingResult.Trial(sessionManager.getTrialDaysRemaining())
            }
        }
    }
}
