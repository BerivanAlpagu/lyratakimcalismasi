package com.turkcell.lyraapp.data.profile

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeProfileRepository @Inject constructor() : ProfileRepository {
    override suspend fun getProfile(): Result<UserProfile> {
        delay(600)
        return Result.success(
            UserProfile(
                id = "u_1",
                name = "Zeynep Kaya",
                username = "@zeynepk",
                status = "Premium",
                playlistCount = 127,
                followerCount = "1.2B",
                followingCount = 348,
                avatarUrl = null
            )
        )
    }
}
