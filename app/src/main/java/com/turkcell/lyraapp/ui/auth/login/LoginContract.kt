package com.turkcell.lyraapp.ui.auth.login

/**
 * Login ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada toplanmıştır.
 */

/**
 * Ekranın gözlemlenebilir tüm durumu. Tek bir immutable kaynak (single source of truth).
 *
 * [isLoginEnabled] doğrudan kullanıcı tarafından set edilmez; alan değişimlerinde
 * [LoginViewModel] tarafından türetilir.
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginEnabled: Boolean = false,
)

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data object Submit : LoginIntent

    /** "Kayıt ol" bağlantısı: Register ekranına geçiş niyeti. */
    data object RegisterClicked : LoginIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb. State içinde tutulmaz,
 * böylece konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface LoginEffect {
    /** Giriş başarılı; ana akışa geç. */
    data object NavigateToHome : LoginEffect

    /** "Kayıt ol" bağlantısı: Register ekranına geç. */
    data object NavigateToRegister : LoginEffect

    data class ShowError(val message: String) : LoginEffect
}
