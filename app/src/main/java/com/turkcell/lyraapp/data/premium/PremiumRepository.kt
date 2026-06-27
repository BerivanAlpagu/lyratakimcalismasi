package com.turkcell.lyraapp.data.premium

interface PremiumRepository {
    suspend fun getPlans(): Result<List<PremiumPlan>>

    suspend fun checkout(
        planType: PremiumPlanType,
        card: PremiumCard,
    ): Result<PremiumCheckoutResult>
}

data class PremiumPlan(
    val id: String,
    val type: PremiumPlanType,
    val name: String,
    val description: String?,
    val priceKurus: Int,
    val price: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean,
)

enum class PremiumPlanType(val apiValue: String) {
    OneTime("one-time"),
    Recurring("recurring"),
}

data class PremiumCard(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String,
)

data class PremiumCheckoutResult(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String,
    val membershipExpiresAt: String,
)
