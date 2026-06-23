package com.turkcell.lyraapp.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * LyraApp ikon seti.
 *
 * Material Icons bağımlılığı eklemeden, ekranların ihtiyaç duyduğu glyph'leri
 * 24x24 viewport'lu [ImageVector] olarak tanımlar. Path'in dolgu rengi önemsizdir;
 * `Icon(...)` composable'ı `tint` ile üzerine yazar. Bu yüzden tüm path'ler
 * [Color.Black] ile doldurulur ve renk daima çağrı tarafında temadan okunur.
 */
object LyraIcons {

    /** Marka logosu: ekolayzer/dalga formu çubukları (Material GraphicEq). */
    val Waveform: ImageVector by lazy {
        lyraIcon(
            name = "Waveform",
            pathData = "M7,18h2V6H7v12zM11,22h2V2h-2v20zM3,14h2v-4H3v4zM15,18h2V6h-2v12zM19,10v4h2v-4h-2z",
        )
    }

    /** Telefon numarası alanının leading ikonu (Material Smartphone, outlined). */
    val Smartphone: ImageVector by lazy {
        lyraIcon(
            name = "Smartphone",
            pathData = "M15.5,1h-8C6.12,1 5,2.12 5,3.5v17C5,21.88 6.12,23 7.5,23h8c1.38,0 " +
                "2.5,-1.12 2.5,-2.5v-17C18,2.12 16.88,1 15.5,1zM13,21h-3v-1h3v1zM16.25,18H6.75V4h9.5V18z",
        )
    }

    /** Şifre alanının leading ikonu (Material Lock). */
    val Lock: ImageVector by lazy {
        lyraIcon(
            name = "Lock",
            pathData = "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0," +
                "1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 " +
                "-2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 " +
                "1.71,0 3.1,1.39 3.1,3.1v2z",
        )
    }

    /** Şifre görünürlük (göz) ikonu (Material Visibility). */
    val Visibility: ImageVector by lazy {
        lyraIcon(
            name = "Visibility",
            pathData = "M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73," +
                "-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 " +
                "-5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z",
        )
    }

    /** Giriş butonu ileri oku (Material ArrowForward). */
    val ArrowForward: ImageVector by lazy {
        lyraIcon(
            name = "ArrowForward",
            pathData = "M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z",
        )
    }

    /** Üst bardaki geri oku (Material ArrowBack). */
    val ArrowBack: ImageVector by lazy {
        lyraIcon(
            name = "ArrowBack",
            pathData = "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z",
        )
    }

    /** Ana sayfa üst bölümündeki tema (açık mod / güneş) ikonu (Material LightMode). */
    val LightMode: ImageVector by lazy {
        lyraIcon(
            name = "LightMode",
            pathData = "M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2," +
                "13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1L2,11c-0.55,0 -1,0.45 -1,1s0.45,1 1,1z" +
                "M20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1z" +
                "M11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1L13,2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1z" +
                "M11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1z" +
                "M5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06," +
                "1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c" +
                "-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39," +
                "0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c" +
                "0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c" +
                "-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39," +
                "-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39," +
                "0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z",
        )
    }

    // ── Oynatıcı (PlayerScreen) kontrol ikonları ──

    /** Oynat (Material PlayArrow, filled). */
    val Play: ImageVector by lazy {
        lyraIcon(
            name = "Play",
            pathData = "M8,5v14l11,-7z",
        )
    }

    /** Duraklat (Material Pause). */
    val Pause: ImageVector by lazy {
        lyraIcon(
            name = "Pause",
            pathData = "M6,19h4V5H6v14zM14,5v14h4V5h-4z",
        )
    }

    /** Baştan başlat (Material Replay; dairesel geri ok). */
    val Restart: ImageVector by lazy {
        lyraIcon(
            name = "Restart",
            pathData = "M12,5V1L7,6l5,5V7c3.31,0 6,2.69 6,6s-2.69,6 -6,6 -6,-2.69 -6,-6H4c0,4.42 " +
                "3.58,8 8,8s8,-3.58 8,-8 -3.58,-8 -8,-8z",
        )
    }

    /** 10 saniye geri sar (Material FastRewind glyph'i). */
    val Rewind10: ImageVector by lazy {
        lyraIcon(
            name = "Rewind10",
            pathData = "M11,18V6l-8.5,6 8.5,6zM11.5,12l8.5,6V6l-8.5,6z",
        )
    }

    /** 10 saniye ileri sar (Material FastForward glyph'i). */
    val Forward10: ImageVector by lazy {
        lyraIcon(
            name = "Forward10",
            pathData = "M4,18l8.5,-6L4,6v12zM13,6v12l8.5,-6L13,6z",
        )
    }

    // ── Alt gezinme çubuğu (LyraBottomBar) ikonları ──

    /** Ana sayfa sekmesi, seçili durum (Material Home, filled). */
    val Home: ImageVector by lazy {
        lyraIcon(
            name = "Home",
            pathData = "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z",
        )
    }

    /** Ana sayfa sekmesi, seçimsiz durum (Material Home, outlined). */
    val HomeOutlined: ImageVector by lazy {
        lyraIcon(
            name = "HomeOutlined",
            pathData = "M12,5.69l5,4.5V18h-2v-6H9v6H7v-7.81l5,-4.5M12,3L2,12h3v8h6v-6h2v6h6v-8h3L12,3z",
        )
    }

    /** Ara sekmesi (Material Search; dolu/outlined varyantı aynıdır). */
    val Search: ImageVector by lazy {
        lyraIcon(
            name = "Search",
            pathData = "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 " +
                "9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5," +
                "4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 " +
                "11.99,14 9.5,14z",
        )
    }

    /** Kütüphane sekmesi, seçili durum (Material LibraryMusic, filled). */
    val LibraryMusic: ImageVector by lazy {
        lyraIcon(
            name = "LibraryMusic",
            pathData = "M20,2H8C6.9,2 6,2.9 6,4v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4c0,-1.1 " +
                "-0.9,-2 -2,-2zM18,7h-3v5.5c0,1.38 -1.12,2.5 -2.5,2.5S10,13.88 10,12.5 11.12,10 " +
                "12.5,10c0.57,0 1.08,0.19 1.5,0.51V5h4v2zM4,6H2v14c0,1.1 0.9,2 2,2h14v-2H4V6z",
        )
    }

    /** Kütüphane sekmesi, seçimsiz durum (Material LibraryMusic, outlined). */
    val LibraryMusicOutlined: ImageVector by lazy {
        lyraIcon(
            name = "LibraryMusicOutlined",
            pathData = "M18,7h-3v5.5c0,1.38 -1.12,2.5 -2.5,2.5S10,13.88 10,12.5 11.12,10 12.5,10c0.57," +
                "0 1.08,0.19 1.5,0.51V5h4V7zM20,4v12H8V4H20M20,2H8C6.9,2 6,2.9 6,4v12c0,1.1 0.9,2 2," +
                "2h12c1.1,0 2,-0.9 2,-2V4C22,2.9 21.1,2 20,2L20,2zM4,6H2v14c0,1.1 0.9,2 2,2h14v-2H4V6z",
        )
    }

    /** Favoriler sekmesi, seçili durum (Material Favorite, filled). */
    val Favorite: ImageVector by lazy {
        lyraIcon(
            name = "Favorite",
            pathData = "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 " +
                "3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4," +
                "6.86 -8.55,11.54L12,21.35z",
        )
    }

    /** Favoriler sekmesi, seçimsiz durum (Material FavoriteBorder). */
    val FavoriteOutlined: ImageVector by lazy {
        lyraIcon(
            name = "FavoriteOutlined",
            pathData = "M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3 4.42,3 2,5.42 " +
                "2,8.5c0,3.78 3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32C18.6,15.36 22,12.28 22,8.5 " +
                "22,5.42 19.58,3 16.5,3zM12.1,18.55l-0.1,0.1 -0.1,-0.1C7.14,14.24 4,11.39 4,8.5 " +
                "4,6.5 5.5,5 7.5,5c1.54,0 3.04,0.99 3.57,2.36h1.87C13.46,5.99 14.96,5 16.5,5c2,0 " +
                "3.5,1.5 3.5,3.5 0,2.89 -3.14,5.74 -7.9,10.05z",
        )
    }

    /** Profil sekmesi, seçili durum (Material Person, filled). */
    val Person: ImageVector by lazy {
        lyraIcon(
            name = "Person",
            pathData = "M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c" +
                "-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z",
        )
    }

    /** Profil sekmesi, seçimsiz durum (Material Person, outlined). */
    val PersonOutlined: ImageVector by lazy {
        lyraIcon(
            name = "PersonOutlined",
            pathData = "M12,5.9c1.16,0 2.1,0.94 2.1,2.1s-0.94,2.1 -2.1,2.1S9.9,9.16 9.9,8s0.94,-2.1 " +
                "2.1,-2.1zM12,14.9c2.97,0 6.1,1.46 6.1,2.1v1.1H5.9V17c0,-0.64 3.13,-2.1 6.1,-2.1zM12," +
                "4C9.79,4 8,5.79 8,8s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4zM12,13c-2.67,0 -8,1.34 " +
                "-8,4v3h16v-3c0,-2.66 -5.33,-4 -8,-4z",
        )
    }

    /** Onay (checkmark) ikonu (Material Check). */
    val Check: ImageVector by lazy {
        lyraIcon(
            name = "Check",
            pathData = "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z",
        )
    }
    /** Favoriler menüsü (Material MoreVert). */
    val MoreVert: ImageVector by lazy {
        lyraIcon(
            name = "MoreVert",
            pathData = "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
        )
    }

    /** Ayarlar (Material Settings). */
    val Settings: ImageVector by lazy {
        lyraIcon(
            name = "Settings",
            pathData = "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94 0,-0.32 -0.02,-0.64 -0.06,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.73,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.06,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.43,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.49 -0.12,-0.61l-2.01,-1.58zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6 3.6,1.62 3.6,3.6 -1.62,3.6 -3.6,3.6z",
        )
    }

    /** Sağa Ok (Material KeyboardArrowRight). */
    val KeyboardArrowRight: ImageVector by lazy {
        lyraIcon(
            name = "KeyboardArrowRight",
            pathData = "M8.59,16.59L13.17,12 8.59,7.41 10,6l6,6 -6,6 -1.41,-1.41z",
        )
    }
}

/**
 * 24x24 viewport'lu, tek path'li bir [ImageVector] üretir.
 * Path verisi standart SVG/Android `pathData` string formatındadır.
 */
private fun lyraIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()
