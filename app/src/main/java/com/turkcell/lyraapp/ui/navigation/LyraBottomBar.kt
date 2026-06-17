package com.turkcell.lyraapp.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Alt gezinme çubuğundaki üst düzey sekmelerin tek doğruluk kaynağı.
 *
 * Her sekme bir [LyraDestination]'a, bir etikete ve seçili/seçimsiz ikon çiftine
 * eşlenir. Sıralama, çubuktaki görünüm sırasıdır. Yeni bir sekme eklendiğinde
 * buraya bir giriş eklenir; [LyraBottomBar] ve görünürlük kontrolü otomatik uyum sağlar.
 */
enum class LyraBottomBarTab(
    val destination: LyraDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(LyraDestination.Home, "Ana sayfa", LyraIcons.Home, LyraIcons.HomeOutlined),
    Search(LyraDestination.Search, "Ara", LyraIcons.Search, LyraIcons.Search),
    Library(LyraDestination.Library, "Kütüphane", LyraIcons.LibraryMusic, LyraIcons.LibraryMusicOutlined),
    Favorites(LyraDestination.Favorites, "Favoriler", LyraIcons.Favorite, LyraIcons.FavoriteOutlined),
    Profile(LyraDestination.Profile, "Profil", LyraIcons.Person, LyraIcons.PersonOutlined),
}

/** [route]'un alt gezinme çubuğu gösterilen bir üst düzey sekme rotası olup olmadığı. */
fun isTopLevelRoute(route: String?): Boolean =
    LyraBottomBarTab.entries.any { it.destination.route == route }

/**
 * Uygulamanın alt gezinme çubuğu (bottom navigation bar).
 *
 * Navigasyon iskeletinin (chrome) parçasıdır; durum sahibi değildir ve MVI sözleşmesi
 * gerektirmez: seçili sekme [currentRoute] üzerinden (nav back stack'ten) türetilir,
 * tıklamalar [onTabSelected] ile yukarı yayılır. Renkler temadan okunur; seçili öğe
 * `primaryContainer` hap göstergesi ile vurgulanır.
 */
@Composable
fun LyraBottomBar(
    currentRoute: String?,
    onTabSelected: (LyraDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        LyraBottomBarTab.entries.forEach { tab ->
            val selected = currentRoute == tab.destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Preview(name = "BottomBar - Dark", showBackground = true)
@Composable
private fun LyraBottomBarDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        LyraBottomBar(
            currentRoute = LyraDestination.Home.route,
            onTabSelected = {},
        )
    }
}

@Preview(name = "BottomBar - Light", showBackground = true)
@Composable
private fun LyraBottomBarLightPreview() {
    LyraAppTheme(darkTheme = false) {
        LyraBottomBar(
            currentRoute = LyraDestination.Search.route,
            onTabSelected = {},
        )
    }
}
