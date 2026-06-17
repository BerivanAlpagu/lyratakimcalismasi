package com.turkcell.lyraapp.data.auth

/**
 * Kimlik doğrulama işlemlerinin tek soyutlama noktası.
 *
 * Backend ekibinin REST API'si henüz tanımlı olmadığından, şu an yalnızca sahte bir
 * implementasyon ([FakeAuthRepository]) ile sağlanır. Gerçek API geldiğinde yalnızca
 * implementasyon değişir; çağıran katmanlar (ör. LoginViewModel) etkilenmez.
 */
interface AuthRepository {

    /**
     * Verilen telefon numarası ve şifreyle giriş dener.
     *
     * @return Başarılıysa [Result.success], aksi halde hata mesajı taşıyan [Result.failure].
     */
    suspend fun login(phoneNumber: String, password: String): Result<Unit>

    /**
     * Verilen kullanıcı bilgileriyle yeni bir hesap oluşturmayı dener.
     *
     * @return Başarılıysa [Result.success], aksi halde hata mesajı taşıyan [Result.failure].
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit>
}

