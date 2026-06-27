package com.turkcell.lyraapp.data.premium

import com.turkcell.lyraapp.data.auth.MembershipDto
import kotlinx.serialization.Serializable

@Serializable
data class MembershipPlanDto(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val priceKurus: Int,
    val price: Double,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean
)

@Serializable
data class MembershipPlansResponseDto(
    val data: List<MembershipPlanDto>
)

@Serializable
data class CardDetailsDto(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String? = null
)

@Serializable
data class CheckoutRequestDto(
    val plan: String,
    val card: CardDetailsDto
)

@Serializable
data class CheckoutResponseDto(
    val data: CheckoutDataDto
)

@Serializable
data class CheckoutDataDto(
    val payment: PaymentDto,
    val membership: MembershipDto
)

@Serializable
data class PaymentDto(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String
)

data class PremiumPlan(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String
)

data class PremiumCheckoutResult(
    val success: Boolean,
    val message: String? = null
)
