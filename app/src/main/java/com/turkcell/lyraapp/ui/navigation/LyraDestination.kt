package com.turkcell.lyraapp.ui.navigation

/**
 * Uygulamadaki navigasyon hedeflerinin tek doğruluk kaynağı.
 *
 * Her hedef benzersiz bir [route] string'iyle temsil edilir; [LyraNavHost] bu route'lar
 * üzerinden composable'ları bağlar. Yeni bir ekran eklendiğinde buraya bir hedef eklenir.
 */
enum class LyraDestination(val route: String) {
    Login("login"),
    Register("register"),
    Otp("otp"),
    ProfileComplete("profile_complete"),
    Home("home"),
    Search("search"),
    Library("library"),
    Favorites("favorites"),
    Profile("profile"),

    /**
     * Oynatıcı ekranı. Üst düzey sekme değildir (alt çubuk gösterilmez); şarkı kimliği yol
     * parametresi, başlık/sanatçı ise opsiyonel query parametresi olarak taşınır.
     * Tam route deseni ve gerçek yol üretimi için bkz. [LyraNavHost].
     */
    Player("player"),
}
