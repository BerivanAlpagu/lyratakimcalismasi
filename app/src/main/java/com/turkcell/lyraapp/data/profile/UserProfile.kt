package com.turkcell.lyraapp.data.profile

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val status: String,
    val playlistCount: Int,
    val followerCount: String,
    val followingCount: Int,
    val avatarUrl: String? = null
)
