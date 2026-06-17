package com.turkcell.lyraapp.ui.player

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
}
