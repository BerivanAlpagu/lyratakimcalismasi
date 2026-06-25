package com.turkcell.lyraapp.data.profile

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(firstName: String, lastName: String, birthDate: String): Result<UserProfile>
}
