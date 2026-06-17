package com.turkcell.lyraapp.data.auth

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [AuthRepository]'nin sahte (stub) implementasyonu.
 *
 * Gerçek bir ağ çağrısı yapmaz; yalnızca MVI akışının uçtan uca çalışmasını sağlamak için
 * bir gecikme ile ağ davranışını taklit eder. Gerçek API geldiğinde bu sınıf bir
 * ağ tabanlı implementasyonla değiştirilir.
 */
class FakeAuthRepository @Inject constructor() : AuthRepository {

    override suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (password.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Şifre boş olamaz."))
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (firstName.isNotBlank() && lastName.isNotBlank() && password.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Hesap bilgileri eksik."))
        }
    }

    private companion object {
        const val NETWORK_DELAY_MS = 1_000L
    }
}
