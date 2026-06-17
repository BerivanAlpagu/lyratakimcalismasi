# LyraApp - Renk Sistemi 

> Bu dosya LyraApp isimli uygulamanın renk paleti için 
> **tek doğruluk kaynağıdır** (single source of truth) ve
> doğrudan bir **Android Jetpack Compose** projesinde kullanılmak
> üzere düzenlenmiştir.


---

## 1. Temel Kural

> Hiçbir `@Composable` içinde ham `Color(0XFF)` yazılmaz. 
> Renkler daima `MaterialTheme.colorSchema.<slot>` üzerinden 
> okunmak zorundadır.

Ham `Color(..)` tanımı yalnızca `Color.kt` içinde, sabit değişken tanımlanırken kullanılır.

---

## 1. `Color.kt` — Ham Token Tanımları

```kotlin
package com.lyraapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── DARK (varsayılan / showcase) ──
val DarkPrimary            = Color(0xFFFFB1C8)
val DarkOnPrimary          = Color(0xFF5E1133)
val DarkPrimaryContainer   = Color(0xFF7B2949)
val DarkOnPrimaryContainer = Color(0xFFFFD9E2)

val DarkSecondary            = Color(0xFFE3BDC6)
val DarkOnSecondary          = Color(0xFF422931)
val DarkSecondaryContainer   = Color(0xFF5B3F47)
val DarkOnSecondaryContainer = Color(0xFFFFD9E2)

val DarkTertiary            = Color(0xFFEFBD94)
val DarkOnTertiary          = Color(0xFF48290B)
val DarkTertiaryContainer   = Color(0xFF633F1F)
val DarkOnTertiaryContainer = Color(0xFFFFDCC1)

val DarkError              = Color(0xFFFFB4AB)
val DarkOnError            = Color(0xFF690005)
val DarkErrorContainer     = Color(0xFF93000A) // türetilen (bkz. §5)
val DarkOnErrorContainer   = Color(0xFFFFDAD6) // türetilen (bkz. §5)

val DarkSurface                  = Color(0xFF191114)
val DarkSurfaceDim               = Color(0xFF191114)
val DarkSurfaceBright            = Color(0xFF41373A)
val DarkSurfaceContainerLowest   = Color(0xFF140C0F)
val DarkSurfaceContainerLow      = Color(0xFF221A1D)
val DarkSurfaceContainer         = Color(0xFF271D20)
val DarkSurfaceContainerHigh     = Color(0xFF31272A)
val DarkSurfaceContainerHighest  = Color(0xFF3C3235)

val DarkOnSurface         = Color(0xFFF0DEE2)
val DarkOnSurfaceVariant  = Color(0xFFD5C2C6)
val DarkSurfaceVariant    = Color(0xFF514347) // türetilen (bkz. §5)
val DarkOutline           = Color(0xFF9E8C90)
val DarkOutlineVariant    = Color(0xFF514347)
val DarkInverseSurface    = Color(0xFFF0DEE2)
val DarkInverseOnSurface  = Color(0xFF382E31)
val DarkInversePrimary    = Color(0xFF8F4A5F) // türetilen (light primary)
val DarkScrim             = Color(0xFF000000)

// ── LIGHT ──
val LightPrimary            = Color(0xFF8F4A5F)
val LightOnPrimary          = Color(0xFFFFFFFF)
val LightPrimaryContainer   = Color(0xFFFFD9E2)
val LightOnPrimaryContainer = Color(0xFF3A071C)

val LightSecondary            = Color(0xFF74565F)
val LightOnSecondary          = Color(0xFFFFFFFF)
val LightSecondaryContainer   = Color(0xFFFFD9E2)
val LightOnSecondaryContainer = Color(0xFF2B151C)

val LightTertiary            = Color(0xFF7C5635)
val LightOnTertiary          = Color(0xFFFFFFFF)
val LightTertiaryContainer   = Color(0xFFFFDCC1)
val LightOnTertiaryContainer = Color(0xFF2D1700)

val LightError              = Color(0xFFBA1A1A)
val LightOnError            = Color(0xFFFFFFFF)
val LightErrorContainer     = Color(0xFFFFDAD6) // türetilen (bkz. §5)
val LightOnErrorContainer   = Color(0xFF410002) // türetilen (bkz. §5)

val LightSurface                  = Color(0xFFFFF8F8)
val LightSurfaceDim               = Color(0xFFE7D6DA)
val LightSurfaceBright            = Color(0xFFFFF8F8)
val LightSurfaceContainerLowest   = Color(0xFFFFFFFF)
val LightSurfaceContainerLow      = Color(0xFFFFF0F2)
val LightSurfaceContainer         = Color(0xFFFBEAEE)
val LightSurfaceContainerHigh     = Color(0xFFF5E4E8)
val LightSurfaceContainerHighest  = Color(0xFFEFDEE2)

val LightOnSurface        = Color(0xFF22191C)
val LightOnSurfaceVariant = Color(0xFF514347)
val LightSurfaceVariant   = Color(0xFFD6C2C6) // türetilen (bkz. §5)
val LightOutline          = Color(0xFF837377)
val LightOutlineVariant   = Color(0xFFD6C2C6)
val LightInverseSurface   = Color(0xFF382E31)
val LightInverseOnSurface = Color(0xFFFEEDF0)
val LightInversePrimary   = Color(0xFFFFB1C8) // türetilen (dark primary)
val LightScrim            = Color(0xFF000000)
```

---

## 2. `Theme.kt` — ColorScheme + MaterialTheme

```kotlin
package com.lyraapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LyraDarkColors = darkColorScheme(
    primary              = DarkPrimary,
    onPrimary            = DarkOnPrimary,
    primaryContainer     = DarkPrimaryContainer,
    onPrimaryContainer   = DarkOnPrimaryContainer,
    secondary            = DarkSecondary,
    onSecondary          = DarkOnSecondary,
    secondaryContainer   = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary             = DarkTertiary,
    onTertiary           = DarkOnTertiary,
    tertiaryContainer    = DarkTertiaryContainer,
    onTertiaryContainer  = DarkOnTertiaryContainer,
    error                = DarkError,
    onError              = DarkOnError,
    errorContainer       = DarkErrorContainer,
    onErrorContainer     = DarkOnErrorContainer,
    background           = DarkSurface,        // türetilen: surface ile aynı
    onBackground         = DarkOnSurface,      // türetilen: onSurface ile aynı
    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = DarkOnSurfaceVariant,
    surfaceTint          = DarkPrimary,        // M3 varsayılanı = primary
    surfaceDim           = DarkSurfaceDim,
    surfaceBright        = DarkSurfaceBright,
    surfaceContainerLowest  = DarkSurfaceContainerLowest,
    surfaceContainerLow     = DarkSurfaceContainerLow,
    surfaceContainer        = DarkSurfaceContainer,
    surfaceContainerHigh    = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,
    inverseSurface       = DarkInverseSurface,
    inverseOnSurface     = DarkInverseOnSurface,
    inversePrimary       = DarkInversePrimary,
    scrim                = DarkScrim,
)

private val LyraLightColors = lightColorScheme(
    primary              = LightPrimary,
    onPrimary            = LightOnPrimary,
    primaryContainer     = LightPrimaryContainer,
    onPrimaryContainer   = LightOnPrimaryContainer,
    secondary            = LightSecondary,
    onSecondary          = LightOnSecondary,
    secondaryContainer   = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary             = LightTertiary,
    onTertiary           = LightOnTertiary,
    tertiaryContainer    = LightTertiaryContainer,
    onTertiaryContainer  = LightOnTertiaryContainer,
    error                = LightError,
    onError              = LightOnError,
    errorContainer       = LightErrorContainer,
    onErrorContainer     = LightOnErrorContainer,
    background           = LightSurface,
    onBackground         = LightOnSurface,
    surface              = LightSurface,
    onSurface            = LightOnSurface,
    surfaceVariant       = LightSurfaceVariant,
    onSurfaceVariant     = LightOnSurfaceVariant,
    surfaceTint          = LightPrimary,
    surfaceDim           = LightSurfaceDim,
    surfaceBright        = LightSurfaceBright,
    surfaceContainerLowest  = LightSurfaceContainerLowest,
    surfaceContainerLow     = LightSurfaceContainerLow,
    surfaceContainer        = LightSurfaceContainer,
    surfaceContainerHigh    = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline              = LightOutline,
    outlineVariant       = LightOutlineVariant,
    inverseSurface       = LightInverseSurface,
    inverseOnSurface     = LightInverseOnSurface,
    inversePrimary       = LightInversePrimary,
    scrim                = LightScrim,
)

@Composable
fun LyraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // KURAL: Dynamic Color KAPALI. Marka paleti her cihazda sabit kalmalı.
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LyraDarkColors else LyraLightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LyraTypography, // ayrı Type.kt
        content = content,
    )
}
```

> **Varsayılan tema:** LyraApp varsayılan olarak DarkTheme olarak açılır, kullanıcı bunu değiştirmekte özgürdür.

---