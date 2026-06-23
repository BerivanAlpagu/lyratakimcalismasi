package com.turkcell.lyraapp.data.profile

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
}
