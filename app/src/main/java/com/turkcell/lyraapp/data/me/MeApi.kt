package com.turkcell.lyraapp.data.me

import com.turkcell.lyraapp.data.auth.UserDto
import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistsResponseDto
import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MeApi {

    @GET("api/v1/me")
    suspend fun getMe(): MeResponseDto

    @POST("api/v1/me/update-informations")
    suspend fun updateInformations(@Body request: UpdateInformationsDto): UpdateInformationsResponseDto

    @GET("api/v1/me/for-you")
    suspend fun getForYou(@Query("limit") limit: Int? = null): MeSongsResponseDto

    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(@Query("limit") limit: Int? = null): MeSongsResponseDto

    @POST("api/v1/me/plays")
    suspend fun recordPlay(@Body request: RecordPlayDto)

    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(): PlaylistsResponseDto

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): CreatePlaylistResponse
}

@Serializable
data class RecordPlayDto(
    val songId: String
)

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

@Serializable
data class MeResponseDto(
    val data: UserDto
)

@Serializable
data class CreatePlaylistRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreatePlaylistResponse(
    val data: PlaylistDto
)
