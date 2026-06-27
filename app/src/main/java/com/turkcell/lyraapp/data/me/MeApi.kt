package com.turkcell.lyraapp.data.me

import com.turkcell.lyraapp.data.auth.UserDto
import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistsResponseDto
import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    @POST("api/v1/me/playback/next")
    suspend fun resolveNextPlayback(@Body request: PlaybackNextRequest): PlaybackNextResponse

    @POST("api/v1/me/playback/ad-complete")
    suspend fun completeAd(@Body request: AdCompleteRequest): AdCompleteResponse

    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(): PlaylistsResponseDto

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): CreatePlaylistResponse

    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addSongToPlaylist(
        @Path("id") playlistId: String,
        @Body request: AddSongRequest,
    ): AddSongResponse

    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("id") playlistId: String,
        @Path("songId") songId: String,
    ): RemoveSongResponse

    @DELETE("api/v1/me/playlists/{id}")
    suspend fun deletePlaylist(
        @Path("id") playlistId: String,
    ): DeletePlaylistResponse

    @POST("api/v1/me/playback/next")
    suspend fun getPlaybackNext(@Body request: PlaybackNextRequestDto): PlaybackNextResponseDto

    @POST("api/v1/me/playback/ad-complete")
    suspend fun completeAd(@Body request: AdCompleteRequestDto): AdCompleteResponseDto
}

@Serializable
data class RecordPlayDto(
    val songId: String
)

@Serializable
data class PlaybackNextRequest(
    val songId: String
)

@Serializable
data class PlaybackNextResponse(
    val data: PlaybackNextDto
)

@Serializable
data class PlaybackNextDto(
    val type: String,
    val song: SongDto,
    val stream: StreamLinkDto,
    val ad: AdDto? = null,
    val adStream: StreamLinkDto? = null,
    val impressionId: String? = null,
)

@Serializable
data class StreamLinkDto(
    val url: String,
    val expiresAt: String? = null,
    val mimeType: String? = null,
)

@Serializable
data class AdDto(
    val id: String,
    val title: String,
    val advertiser: String,
    val durationMs: Long,
    val mimeType: String? = null,
)

@Serializable
data class AdCompleteRequest(
    val impressionId: String
)

@Serializable
data class AdCompleteResponse(
    val data: AdCompleteData
)

@Serializable
data class AdCompleteData(
    val completed: Boolean
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

@Serializable
data class AddSongRequest(
    val songId: String
)

@Serializable
data class AddSongResponse(
    val data: AddSongData
)

@Serializable
data class AddSongData(
    val added: Boolean
)

@Serializable
data class RemoveSongResponse(
    val data: RemoveSongData
)

@Serializable
data class RemoveSongData(
    val removed: Boolean
)

@Serializable
data class PlaybackNextRequestDto(val songId: String)

@Serializable
data class PlaybackNextResponseDto(
    val data: PlaybackItemDto
)

@Serializable
sealed class PlaybackItemDto {
    @Serializable
    @kotlinx.serialization.SerialName("song")
    data class SongPlayback(
        val song: SongDto,
        val streamUrl: String
    ) : PlaybackItemDto()

    @Serializable
    @kotlinx.serialization.SerialName("ad")
    data class AdPlayback(
        val ad: AdDto,
        val impressionId: String,
        val song: SongDto,
        val songStreamUrl: String
    ) : PlaybackItemDto()
}

@Serializable
data class AdDto(
    val id: String,
    val title: String,
    val streamUrl: String,
    val durationMs: Long
)

@Serializable
data class AdCompleteRequestDto(val impressionId: String)

@Serializable
data class AdCompleteResponseDto(val data: AdCompleteDataDto)

@Serializable
data class AdCompleteDataDto(val completed: Boolean)

@Serializable
data class DeletePlaylistResponse(
    val data: DeletePlaylistData
)

@Serializable
data class DeletePlaylistData(
    val deleted: Boolean
)
