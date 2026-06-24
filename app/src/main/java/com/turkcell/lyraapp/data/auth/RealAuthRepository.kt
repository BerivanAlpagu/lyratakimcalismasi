package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.local.TokenStore
import com.turkcell.lyraapp.data.me.MeApi
import com.turkcell.lyraapp.data.me.UpdateInformationsDto
import javax.inject.Inject

class RealAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val meApi: MeApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun requestOtp(phone: String): Result<OtpRequestData> = runCatching {
        val response = authApi.requestOtp(OtpRequestDto(phone))
        response.data
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<AuthSessionDto> = runCatching {
        val response = authApi.verifyOtp(OtpVerifyDto(phone, code))
        val session = response.data
        
        // Save tokens
        tokenStore.save(session.accessToken, session.refreshToken)
        
        session
    }

    override suspend fun updateProfile(firstName: String, lastName: String, birthDate: String): Result<UserDto> = runCatching {
        val response = meApi.updateInformations(UpdateInformationsDto(firstName, lastName, birthDate))
        response.data
    }
}
