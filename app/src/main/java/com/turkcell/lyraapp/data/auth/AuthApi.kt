package com.turkcell.lyraapp.data.auth

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body request: OtpRequestDto): OtpRequestResponseDto

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyDto): OtpVerifyResponseDto
}

@Serializable
data class OtpRequestDto(val phone: String)

@Serializable
data class OtpRequestResponseDto(val data: OtpRequestData)

@Serializable
data class OtpRequestData(
    val sent: Boolean,
    val firstTime: Boolean
)

@Serializable
data class OtpVerifyDto(
    val phone: String,
    val code: String
)

@Serializable
data class OtpVerifyResponseDto(val data: AuthSessionDto)

@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val firstTime: Boolean,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val phone: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val profileCompleted: Boolean,
    val membership: MembershipDto? = null
)

@Serializable
data class MembershipDto(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val startedAt: String,
    val expiresAt: String? = null
)
