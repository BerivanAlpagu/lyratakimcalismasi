package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.auth.MembershipDto
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
            name = "${firstName ?: ""} ${lastName ?: ""}".trim().ifEmpty { "İsimsiz Kullanıcı" },
            username = "@${firstName?.lowercase() ?: "user"}",
            status = membership?.toStatusLabel() ?: "Free",
            playlistCount = 127, // Mocked
            followerCount = "1.2B", // Mocked
            followingCount = 348, // Mocked
            avatarUrl = null,
            membership = membership?.toUserMembership()
        )
    }

    private fun MembershipDto.toStatusLabel(): String =
        when (status.lowercase()) {
            "active" -> "Premium"
            "expired" -> "Free"
            else -> "Free"
        }

    private fun MembershipDto.toUserMembership(): UserMembership =
        UserMembership(
            planId = planId,
            type = when (type.lowercase()) {
                "one-time" -> MembershipType.OneTime
                "recurring" -> MembershipType.Recurring
                else -> MembershipType.Unknown
            },
            status = when (status.lowercase()) {
                "active" -> MembershipStatus.Active
                "expired" -> MembershipStatus.Expired
                else -> MembershipStatus.Unknown
            },
            autoRenew = autoRenew,
            startedAt = startedAt,
            expiresAt = expiresAt,
        )
}
