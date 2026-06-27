package com.turkcell.lyraapp.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.otp.OtpRoute
import com.turkcell.lyraapp.ui.auth.profile.ProfileCompleteRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.payment.PaymentRoute
import com.turkcell.lyraapp.ui.payment.PaymentSuccessRoute
import com.turkcell.lyraapp.ui.player.PlayerRoute
import com.turkcell.lyraapp.ui.player.PlayerViewModel
import com.turkcell.lyraapp.ui.premium.PremiumRoute
import com.turkcell.lyraapp.ui.premium.PremiumViewModel
import com.turkcell.lyraapp.ui.search.SearchRoute

/**
 * Uygulamanın iskelet navigasyon yapısı.
 *
 * Tek [NavHost], Auth grafiği ile ana akış sekmelerini barındırır; başlangıç hedefi
 * [LyraDestination.Login]'dir. Dış [Scaffold]'ın `bottomBar` yuvasındaki [LyraBottomBar]
 * yalnızca üst düzey sekme rotalarında görünür; böylece çubuk her ana sayfanın altında
 * yer alır, Auth ekranlarında gizlenir.
 *
 * Her ekranın `Route` composable'ı, MVI Effect'lerini buradan sağlanan navigasyon
 * lambda'larına köprüler (ViewModel navigasyon API'si bilmez; bkz. mvi-viewmodel-rules §6).
 *
 * Dış Scaffold'ın `contentWindowInsets`'i sıfırlanır: sistem çubuğu boşluklarını her ekran
 * kendisi yönetir (Login/Register'da olduğu gibi); içerik dolgusu yalnızca alt çubuğun
 * yüksekliğini taşır.
 */
@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isTopLevelRoute(currentRoute)) {
                LyraBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = navController::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LyraDestination.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LyraDestination.Login.route) {
                LoginRoute(
                    onNavigateToOtp = { phone -> 
                        navController.navigate("${LyraDestination.Otp.route}/$phone") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(
                route = "${LyraDestination.Otp.route}/{phone}",
                arguments = listOf(navArgument("phone") { type = NavType.StringType })
            ) {
                OtpRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToProfileComplete = {
                        navController.navigate(LyraDestination.ProfileComplete.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(LyraDestination.ProfileComplete.route) {
                ProfileCompleteRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() }
                )
            }



            composable(LyraDestination.Home.route) {
                HomeRoute(
                    onSongClick = { songId, title, artist ->
                        navController.navigate(playerRoute(songId, title, artist))
                    },
                )
            }

            // Burası PlaceholderScreen yerine gerçek SearchRoute'a bağlandı
            composable(LyraDestination.Search.route) {
                SearchRoute(
                    onSongClick = { songId, title, artist ->
                        navController.navigate(playerRoute(songId, title, artist))
                    }
                )
            }

            composable(LyraDestination.Library.route) {
                LibraryRoute(
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate("${LyraDestination.PlaylistDetail.route}/$playlistId")
                    },
                    onNavigateToCreatePlaylist = {
                        navController.navigate(LyraDestination.CreatePlaylist.route)
                    }
                )
            }

            composable(
                route = "${LyraDestination.PlaylistDetail.route}/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) {
                com.turkcell.lyraapp.ui.playlist_detail.PlaylistDetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onSongClick = { songId, title, artist ->
                        navController.navigate(playerRoute(songId, title, artist))
                    }
                )
            }

            composable(LyraDestination.CreatePlaylist.route) {
                com.turkcell.lyraapp.ui.create_playlist.CreatePlaylistRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(LyraDestination.Favorites.route) {
                com.turkcell.lyraapp.ui.favorites.FavoritesRoute(
                    onShowSnackbar = {}
                )
            }

            composable(LyraDestination.Profile.route) {
                com.turkcell.lyraapp.ui.profile.ProfileRoute(
                    onShowSnackbar = {},
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremium = { planType ->
                        navController.navigate(premiumRoute(planType.orEmpty()))
                    },
                )
            }

            composable(
                route = PREMIUM_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(PremiumViewModel.ARG_PLAN_TYPE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                PremiumRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPayment = { planType ->
                        navController.navigate(paymentRoute(planType.apiValue))
                    },
                )
            }

            composable(
                route = PAYMENT_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(PremiumViewModel.ARG_PLAN_TYPE) { type = NavType.StringType },
                ),
            ) {
                PaymentRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSuccess = {
                        navController.navigate(LyraDestination.PaymentSuccess.route) {
                            popUpTo(LyraDestination.Premium.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(LyraDestination.PaymentSuccess.route) {
                PaymentSuccessRoute(
                    onStartListening = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(LyraDestination.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = PLAYER_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(PlayerViewModel.ARG_SONG_ID) { type = NavType.StringType },
                    navArgument(PlayerViewModel.ARG_TITLE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(PlayerViewModel.ARG_ARTIST) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(PlayerViewModel.ARG_COVER_URL) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(PlayerViewModel.ARG_PLAYLIST_NAME) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(PlayerViewModel.ARG_IS_FAVORITE) {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
            ) {
                PlayerRoute(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Oynatıcı route deseni: şarkı kimliği yol parametresi, başlık/sanatçı opsiyonel query
 * parametresidir. ViewModel argümanları [PlayerViewModel.ARG_*] ile bu desenden çözer.
 */
private const val PLAYER_ROUTE_PATTERN =
    "player/{${PlayerViewModel.ARG_SONG_ID}}?" +
            "${PlayerViewModel.ARG_TITLE}={${PlayerViewModel.ARG_TITLE}}&" +
            "${PlayerViewModel.ARG_ARTIST}={${PlayerViewModel.ARG_ARTIST}}&" +
            "${PlayerViewModel.ARG_COVER_URL}={${PlayerViewModel.ARG_COVER_URL}}&" +
            "${PlayerViewModel.ARG_PLAYLIST_NAME}={${PlayerViewModel.ARG_PLAYLIST_NAME}}&" +
            "${PlayerViewModel.ARG_IS_FAVORITE}={${PlayerViewModel.ARG_IS_FAVORITE}}"

private const val PREMIUM_ROUTE_PATTERN =
    "premium?${PremiumViewModel.ARG_PLAN_TYPE}={${PremiumViewModel.ARG_PLAN_TYPE}}"

private const val PAYMENT_ROUTE_PATTERN =
    "payment/{${PremiumViewModel.ARG_PLAN_TYPE}}"

/**
 * Bir şarkı için gerçek oynatıcı yolunu üretir. Tüm bileşenler URL-encode edilir; böylece
 * boşluk/özel karakter içeren başlık ve sanatçı adları route'u bozmaz.
 */
private fun playerRoute(
    songId: String, 
    title: String = "", 
    artist: String = "",
    coverUrl: String = "",
    playlistName: String = "",
    isFavorite: Boolean = false
): String =
    "player/${Uri.encode(songId)}?" +
            "${PlayerViewModel.ARG_TITLE}=${Uri.encode(title)}&" +
            "${PlayerViewModel.ARG_ARTIST}=${Uri.encode(artist)}&" +
            "${PlayerViewModel.ARG_COVER_URL}=${Uri.encode(coverUrl)}&" +
            "${PlayerViewModel.ARG_PLAYLIST_NAME}=${Uri.encode(playlistName)}&" +
            "${PlayerViewModel.ARG_IS_FAVORITE}=$isFavorite"

private fun premiumRoute(planType: String = ""): String =
    "premium?${PremiumViewModel.ARG_PLAN_TYPE}=${Uri.encode(planType)}"

private fun paymentRoute(planType: String): String =
    "payment/${Uri.encode(planType)}"

/**
 * Alt çubuk sekmesine standart desenle geçiş yapar: back stack'te sekme kopyası birikmez
 * (`launchSingleTop`), sekmeler arası geçişte durum saklanır/geri yüklenir
 * (`saveState`/`restoreState`) ve geri tuşu daima Home'a döner (`popUpTo(Home)`).
 */
private fun NavHostController.navigateToTab(destination: LyraDestination) {
    navigate(destination.route) {
        popUpTo(LyraDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Auth akışını back stack'ten temizleyerek Home'a geçer (geri tuşu Login'e dönmez). */
private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(LyraDestination.Home.route) {
        popUpTo(LyraDestination.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}
