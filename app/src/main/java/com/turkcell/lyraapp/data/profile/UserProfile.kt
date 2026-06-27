package com.turkcell.lyraapp.data.profile

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val status: String,
    val playlistCount: Int,
    val followerCount: String,
    val followingCount: Int,
    val avatarUrl: String? = null,
    val membership: UserMembership? = null,
)

data class UserMembership(
    val planId: String,
    val type: MembershipType,
    val status: MembershipStatus,
    val autoRenew: Boolean,
    val startedAt: String?,
    val expiresAt: String,
)

enum class MembershipType {
    OneTime,
    Recurring,
    Unknown,
}

enum class MembershipStatus {
    Active,
    Expired,
    Unknown,
}
