package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.me.MeApi
import com.turkcell.lyraapp.data.playlists.PlaylistsApi
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class DefaultHomeRepository @Inject constructor(
    private val songsApi: SongsApi,
    private val meApi: MeApi,
    private val playlistsApi: PlaylistsApi,
) : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        coroutineScope {
            val meDeferred = async { meApi.getMe() }
            val songsDeferred = async { songsApi.getSongs(limit = SONGS_PAGE_SIZE) }
            val forYouDeferred = async { meApi.getForYou(limit = 6) }
            val recentlyPlayedDeferred = async { meApi.getRecentlyPlayed(limit = 10) }
            val playlistsDeferred = async { playlistsApi.getPlaylists() }

            val meResponse = meDeferred.await()
            val songsResponse = songsDeferred.await()
            val forYouResponse = forYouDeferred.await()
            val recentlyPlayedResponse = recentlyPlayedDeferred.await()
            val playlistsResponse = playlistsDeferred.await()

            val userInitials = meResponse.data.let { user ->
                val first = user.firstName?.firstOrNull()?.uppercase() ?: ""
                val last = user.lastName?.firstOrNull()?.uppercase() ?: ""
                "$first$last"
            }

            val songs = songsResponse.data.map { it.toHomeSong() }
            
            val quickPicks = forYouResponse.data.map { song ->
                val (start, end) = artworkColorsFor(song.id)
                QuickPick(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    artworkStartColor = start,
                    artworkEndColor = end
                )
            }

            val recentlyPlayed = recentlyPlayedResponse.data.map { song ->
                val (start, end) = artworkColorsFor(song.id)
                RecentlyPlayed(
                    id = song.id,
                    title = song.title,
                    subtitle = song.artist,
                    artworkStartColor = start,
                    artworkEndColor = end
                )
            }

            val playlistsForYou = playlistsResponse.data.map { playlist ->
                val (start, end) = artworkColorsFor(playlist.id)
                PlaylistForYou(
                    id = playlist.id,
                    title = playlist.name,
                    artworkStartColor = start,
                    artworkEndColor = end
                )
            }

            HomeFeed(
                userInitials = if (userInitials.isNotBlank()) userInitials else "U",
                songs = songs,
                quickPicks = quickPicks,
                recentlyPlayed = recentlyPlayed,
                playlistsForYou = playlistsForYou,
            )
        }
    }

    private fun SongDto.toHomeSong(): HomeSong {
        val (start, end) = artworkColorsFor(id)
        return HomeSong(
            id = id,
            title = title,
            artist = artist,
            artworkStartColor = start,
            artworkEndColor = end,
        )
    }

    private companion object {
        const val SONGS_PAGE_SIZE = 20

        fun artworkColorsFor(id: String): Pair<Long, Long> {
            val hue = (abs(id.hashCode()) % 360).toFloat()
            val start = hslToArgb(hue, saturation = 0.50f, lightness = 0.55f)
            val end = hslToArgb(hue, saturation = 0.55f, lightness = 0.32f)
            return start to end
        }

        fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Long {
            val c = (1f - abs(2f * lightness - 1f)) * saturation
            val hPrime = hue / 60f
            val x = c * (1f - abs(hPrime % 2f - 1f))
            val (r1, g1, b1) = when {
                hPrime < 1f -> Triple(c, x, 0f)
                hPrime < 2f -> Triple(x, c, 0f)
                hPrime < 3f -> Triple(0f, c, x)
                hPrime < 4f -> Triple(0f, x, c)
                hPrime < 5f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val m = lightness - c / 2f
            val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}
