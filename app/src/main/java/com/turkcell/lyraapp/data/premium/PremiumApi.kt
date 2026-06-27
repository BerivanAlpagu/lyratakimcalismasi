package com.turkcell.lyraapp.data.premium

import com.turkcell.lyraapp.data.auth.MembershipDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PremiumApi {

    @GET("api/v1/memberships/plans")
    suspend fun getPlans(): MembershipPlansResponse

    @POST("api/v1/memberships/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse
}

@Serializable
data class MembershipPlansResponse(
    val data: List<MembershipPlanDto>
)

@Serializable
data class MembershipPlanDto(
    val id: String,
    val type: String,
    val name: String,
    val description: String? = null,
    val priceKurus: Int,
    val price: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean,
)

@Serializable
data class CheckoutRequest(
    val plan: String,
    val card: CheckoutCardDto,
)

@Serializable
data class CheckoutCardDto(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String? = null,
)

@Serializable
data class CheckoutResponse(
    val data: CheckoutDataDto
)

@Serializable
data class CheckoutDataDto(
    val payment: PaymentDto,
    val membership: MembershipDto,
)

@Serializable
data class PaymentDto(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String,
)
