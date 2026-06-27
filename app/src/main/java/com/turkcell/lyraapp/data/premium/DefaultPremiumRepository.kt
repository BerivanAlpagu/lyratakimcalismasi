package com.turkcell.lyraapp.data.premium

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPremiumRepository @Inject constructor(
    private val premiumApi: PremiumApi,
) : PremiumRepository {

    override suspend fun getPlans(): Result<List<PremiumPlan>> = runCatching {
        premiumApi.getPlans().data.map { it.toPremiumPlan() }
    }

    override suspend fun checkout(
        planType: PremiumPlanType,
        card: PremiumCard,
    ): Result<PremiumCheckoutResult> = runCatching {
        val response = premiumApi.checkout(
            CheckoutRequest(
                plan = planType.apiValue,
                card = CheckoutCardDto(
                    number = card.number,
                    expMonth = card.expMonth,
                    expYear = card.expYear,
                    cvc = card.cvc,
                    holderName = card.holderName,
                ),
            ),
        )
        PremiumCheckoutResult(
            transactionId = response.data.payment.transactionId,
            amountKurus = response.data.payment.amountKurus,
            currency = response.data.payment.currency,
            membershipExpiresAt = response.data.membership.expiresAt,
        )
    }

    private fun MembershipPlanDto.toPremiumPlan(): PremiumPlan =
        PremiumPlan(
            id = id,
            type = when (type) {
                PremiumPlanType.OneTime.apiValue -> PremiumPlanType.OneTime
                PremiumPlanType.Recurring.apiValue -> PremiumPlanType.Recurring
                else -> PremiumPlanType.OneTime
            },
            name = name,
            description = description,
            priceKurus = priceKurus,
            price = price,
            currency = currency,
            durationDays = durationDays,
            autoRenew = autoRenew,
        )
}
