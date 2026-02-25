package com.blockremote.data.network

import com.blockremote.data.network.models.AuthTokenRequest
import com.blockremote.data.network.models.AuthTokenResponse
import com.blockremote.data.network.models.BillingStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BlockRemoteApi {

    @GET("v1/billing/status")
    suspend fun getBillingStatus(): Response<BillingStatusResponse>

    @POST("v1/auth/device")
    suspend fun registerDevice(@Body request: AuthTokenRequest): Response<AuthTokenResponse>
}
