package com.turkcell.lyraapp.ui.player

import androidx.compose.ui.graphics.Color

/**
 * Oynatıcı ekranının MVI sözleşmesi: UiState + Intent (bkz. mvi-contracts.md).
 *
 * Durum, ExoPlayer'ın [androidx.media3.common.Player.Listener] geri çağrıları ve periyodik
 * konum güncellemesiyle ViewModel'de tutulur; ekran yalnızca bu durumu çizip kullanıcı
 * etkileşimlerini [PlayerIntent] olarak yukarı yayar. Geri navigasyon Route katmanında
 * ele alındığından ayrı bir Effect tanımına gerek yoktur.
 */
data class PlayerUiState(
    val title: String = "",
    val artist: String = "",
    val playlistName: String = "",
    val coverUrl: String = "",

    /** Stream URL alınana ve oynatıcı hazır olana kadar true. */
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    /** Oynatıcı tampon dolduruyor (buffering); ilerleme çubuğu beklemede gösterilebilir. */
    val isBuffering: Boolean = false,
    /** Parça sonuna ulaşıldı; "yeniden başlat" baskın aksiyon olur. */
    val hasEnded: Boolean = false,

    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,

    val errorMessage: String? = null,

    /** Kullanıcı bu parçayı favorilere ekledi mi. */
    val isFavorite: Boolean = false,
    /** Karıştır modu açık mı. */
    val isShuffling: Boolean = false,
    /**
     * Tekrar modu:
     *  0 → kapalı
     *  1 → tümünü tekrar et
     *  2 → bu parçayı tekrar et
     */
    val repeatMode: Int = 0,

    /**
     * Kapak fotoğrafından Palette API ile çıkarılan baskın renk.
     * Null ise gradient için DarkSurface (#191114) kullanılır.
     */
    val dominantColor: Color? = null,

    /**
     * ExoPlayer kuyruğunda önceki/sonraki öğe mevcut mu.
     * Şu an tek parça yüklendiğinden her ikisi de false'tur;
     * ilerleyen fazda playlist kuyruğu bağlandığında otomatik güncellenir.
     */
    val canSkipPrevious: Boolean = false,
    val canSkipNext: Boolean = false,
)

sealed interface PlayerIntent {
    /** Çalıyorsa duraklat, duraklatılmışsa devam ettir. */
    data object TogglePlayPause : PlayerIntent

    /** Parçayı başa sarıp baştan çalar. */
    data object Restart : PlayerIntent

    /** 10 saniye ileri sarar. */
    data object SeekForward : PlayerIntent

    /** 10 saniye geri sarar. */
    data object SeekBackward : PlayerIntent

    /** İlerleme çubuğundan verilen konuma atlar. */
    data class SeekTo(val positionMs: Long) : PlayerIntent

    /** Stream URL alınamadığında yeniden dener. */
    data object Retry : PlayerIntent

    /** Favori durumunu tersine çevirir. */
    data object ToggleFavorite : PlayerIntent

    /** Karıştır modunu açar / kapatır. */
    data object ToggleShuffle : PlayerIntent

    /**
     * Tekrar modunu döngüsel olarak değiştirir:
     * kapalı → tümünü tekrar → bu parçayı tekrar → kapalı
     */
    data object ToggleRepeat : PlayerIntent

    /** Önceki şarkıya geçer. */
    data object SkipPrevious : PlayerIntent

    /** Sonraki şarkıya geçer. */
    data object SkipNext : PlayerIntent

    /**
     * Kapak bitmap'inden hesaplanan baskın rengi state'e yazar.
     * [color] null ise fallback renk kullanılır.
     */
    data class UpdateDominantColor(val color: Int?) : PlayerIntent
}
