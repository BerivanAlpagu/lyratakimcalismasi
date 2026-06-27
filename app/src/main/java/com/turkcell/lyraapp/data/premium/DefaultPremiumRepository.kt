package com.turkcell.lyraapp.data.premium

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPremiumRepository @Inject constructor(
    private val premiumApi: PremiumApi
) : PremiumRepository {

    override suspend fun getPremiumPlans(): Result<List<PremiumPlan>> = runCatching {
        val response = premiumApi.getPlans()
        response.data.map { planDto ->
            PremiumPlan(
                id = planDto.id,
                type = planDto.type,
                name = planDto.name,
                description = planDto.description,
                price = planDto.price,
                currency = planDto.currency
            )
        }
    }

    override suspend fun checkout(planId: String, card: CardDetailsDto): Result<PremiumCheckoutResult> = runCatching {
        val response = premiumApi.checkout(
            CheckoutRequestDto(
                plan = planId,
                card = card
            )
        )
        PremiumCheckoutResult(
            success = true,
            message = "Premium membership activated successfully"
        )
    }
}
