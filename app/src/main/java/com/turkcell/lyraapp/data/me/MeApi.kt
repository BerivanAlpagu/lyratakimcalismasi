package com.turkcell.lyraapp.data.me

import com.turkcell.lyraapp.data.auth.UserDto
import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MeApi {

    @POST("api/v1/me/update-informations")
    suspend fun updateInformations(@Body request: UpdateInformationsDto): UpdateInformationsResponseDto

    @GET("api/v1/me/for-you")
    suspend fun getForYou(@Query("limit") limit: Int? = null): MeSongsResponseDto

    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(@Query("limit") limit: Int? = null): MeSongsResponseDto
}

@Serializable
data class UpdateInformationsDto(
    val firstName: String,
    val lastName: String,
    val birthDate: String
)

@Serializable
data class UpdateInformationsResponseDto(
    val data: UserDto
)

@Serializable
data class MeSongsResponseDto(
    val data: List<SongDto>
)
