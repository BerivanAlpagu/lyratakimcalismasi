package com.turkcell.lyraapp.data.auth

interface AuthRepository {
    suspend fun requestOtp(phone: String): Result<OtpRequestData>
    
    suspend fun verifyOtp(phone: String, code: String): Result<AuthSessionDto>
    
    suspend fun updateProfile(firstName: String, lastName: String, birthDate: String): Result<UserDto>
}
