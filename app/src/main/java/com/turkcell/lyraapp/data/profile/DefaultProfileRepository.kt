package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.auth.UserDto
import com.turkcell.lyraapp.data.me.MeApi
import com.turkcell.lyraapp.data.me.UpdateInformationsDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val meApi: MeApi
) : ProfileRepository {

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val response = meApi.getMe()
        response.data.toUserProfile()
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<UserProfile> = runCatching {
        val response = meApi.updateInformations(
            UpdateInformationsDto(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate
            )
        )
        response.data.toUserProfile()
    }

    private fun UserDto.toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            phone = phone,
            username = "@${firstName?.lowercase() ?: "user"}",
            status = if (membership?.status == "active") "Premium" else "Free",
            playlistCount = 127, // Mocked
            followerCount = "1.2B", // Mocked
            followingCount = 348, // Mocked
            avatarUrl = null,
            membership = membership?.let {
                Membership(
                    planId = it.planId,
                    type = it.type,
                    status = it.status,
                    autoRenew = it.autoRenew,
                    startedAt = it.startedAt,
                    expiresAt = it.expiresAt
                )
            }
        )
    }
}
