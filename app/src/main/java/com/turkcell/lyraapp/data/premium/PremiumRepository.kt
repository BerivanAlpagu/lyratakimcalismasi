package com.turkcell.lyraapp.data.premium

interface PremiumRepository {
    suspend fun getPremiumPlans(): Result<List<PremiumPlan>>
    suspend fun checkout(planId: String, card: CardDetailsDto): Result<PremiumCheckoutResult>
}
