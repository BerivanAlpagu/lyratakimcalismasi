package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * [HomeRepository]'nin geçiş dönemi implementasyonu.
 *
 * "Şarkılar" bölümünü gerçek API'dan ([SongsApi]) besler; diğer bölümler (quickPicks,
 * recentlyPlayed, playlists) backend hazır olmadığından statik kalır. Şarkı kapakları için
 * API'da görsel olmadığından her şarkıya [id]'sinden türetilen sabit bir gradyan renk çifti
 * atanır (bkz. docs/decisions.md — Şarkı Listesi API Entegrasyonu).
 *
 * Şarkı çağrısı başarısız olursa tüm besleme `Result.failure` döner; UI mevcut "Tekrar dene"
 * akışını yeniden kullanır.
 */
class DefaultHomeRepository @Inject constructor(
    private val songsApi: SongsApi,
) : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        val songs = songsApi.getSongs(limit = SONGS_PAGE_SIZE).data.map { it.toHomeSong() }
        HomeFeed(
            userInitials = USER_INITIALS,
            songs = songs,
            quickPicks = QUICK_PICKS,
            recentlyPlayed = RECENTLY_PLAYED,
            playlistsForYou = PLAYLISTS_FOR_YOU,
        )
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
        const val USER_INITIALS = "ZK"

        /**
         * Şarkı [id]'sinden deterministik bir gradyan renk çifti (başlangıç/bitiş, ARGB) üretir.
         * Ton (hue) id hash'inden türetilir; başlangıç açık, bitiş koyu tonludur. Android'e
         * bağımlı olmamak için HSL→RGB dönüşümü elle yapılır.
         */
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

        val QUICK_PICKS = listOf(
            QuickPick("qp-1", "Gece Sürüşü", 0xFF8B6FB8, 0xFF4A3D6B),
            QuickPick("qp-2", "Sabah Kahvesi", 0xFF7C83D9, 0xFF3E4486),
            QuickPick("qp-3", "Neon Sokaklar", 0xFFD98E4A, 0xFF8A5526),
            QuickPick("qp-4", "Odaklan", 0xFF4AC2A8, 0xFF1F6E5C),
            QuickPick("qp-5", "Derin Mavi", 0xFF6FBF5A, 0xFF356B2A),
            QuickPick("qp-6", "Yaz Anıları", 0xFF5AAFC9, 0xFF2A5F73),
        )

        val RECENTLY_PLAYED = listOf(
            RecentlyPlayed("rp-1", "Neon Sokaklar", "Şehir Işıkları", 0xFFD98E4A, 0xFF8A5526),
            RecentlyPlayed("rp-2", "Derin Mavi", "Okyanus", 0xFF6FBF5A, 0xFF356B2A),
            RecentlyPlayed("rp-3", "Yıldız Tozu", "Polaris", 0xFF3D5A80, 0xFF1B2A45),
        )

        val PLAYLISTS_FOR_YOU = listOf(
            PlaylistForYou("pl-1", "Haftalık Keşif", 0xFF9B7FC4, 0xFF5A4480),
            PlaylistForYou("pl-2", "Sakin Akşamlar", 0xFF6B5FB8, 0xFF3A3270),
            PlaylistForYou("pl-3", "Enerji Ver", 0xFF3FAE9C, 0xFF1E5D52),
        )
    }
}
