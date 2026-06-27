package com.turkcell.lyraapp.data.premium

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PremiumApi {
    @GET("api/v1/memberships/plans")
    suspend fun getPlans(): MembershipPlansResponseDto

    @POST("api/v1/memberships/checkout")
    suspend fun checkout(@Body request: CheckoutRequestDto): CheckoutResponseDto
}
