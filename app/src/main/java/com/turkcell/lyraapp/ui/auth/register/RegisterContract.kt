package com.turkcell.lyraapp.ui.auth.register

/**
 * Register ("Hesap oluştur") ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti)
 * ve Effect (tek seferlik olay) tek dosyada toplanmıştır.
 */

/**
 * Ekranın gözlemlenebilir tüm durumu. Tek bir immutable kaynak (single source of truth).
 *
 * [passwordStrength] ve [isRegisterEnabled] doğrudan kullanıcı tarafından set edilmez;
 * alan değişimlerinde [RegisterViewModel] tarafından türetilir.
 */
data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    /** Şifre gücü göstergesi için 0..[PASSWORD_STRENGTH_MAX] arası türetilmiş değer. */
    val passwordStrength: Int = 0,
    val isRegisterEnabled: Boolean = false,
) {
    companion object {
        /** Şifre güç göstergesindeki segment sayısı (ekran tasarımındaki üç çubuk). */
        const val PASSWORD_STRENGTH_MAX = 3
    }
}

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface RegisterIntent {
    data class FirstNameChanged(val value: String) : RegisterIntent
    data class LastNameChanged(val value: String) : RegisterIntent
    data class PhoneNumberChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class TermsAcceptedChanged(val value: Boolean) : RegisterIntent
    data object TogglePasswordVisibility : RegisterIntent
    data object Submit : RegisterIntent
    data object BackClicked : RegisterIntent
    data object LoginClicked : RegisterIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb. State içinde tutulmaz,
 * böylece konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface RegisterEffect {
    /** Kayıt başarılı; ana akışa geç. */
    data object NavigateToHome : RegisterEffect

    /** "Giriş yap" bağlantısı: Login ekranına geç. */
    data object NavigateToLogin : RegisterEffect

    /** Geri oku: bir önceki ekrana dön. */
    data object NavigateBack : RegisterEffect

    data class ShowError(val message: String) : RegisterEffect
}
