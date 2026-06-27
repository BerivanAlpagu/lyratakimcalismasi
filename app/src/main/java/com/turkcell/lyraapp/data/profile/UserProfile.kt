package com.turkcell.lyraapp.data.profile

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val username: String,
    val status: String,
    val playlistCount: Int,
    val followerCount: String,
    val followingCount: Int,
    val avatarUrl: String? = null,
    val membership: Membership? = null
) {
    val name: String
        get() = "$firstName $lastName".trim()
}

data class Membership(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val startedAt: String,
    val expiresAt: String? = null
)
