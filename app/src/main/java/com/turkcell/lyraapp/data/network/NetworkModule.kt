package com.turkcell.lyraapp.data.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.turkcell.lyraapp.data.playlists.PlaylistsApi
import com.turkcell.lyraapp.data.songs.SongsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton
import com.turkcell.lyraapp.data.local.TokenStore
import com.turkcell.lyraapp.data.auth.AuthApi
import com.turkcell.lyraapp.data.me.MeApi
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Ağ katmanı bağımlılıklarını sağlar: [Json], [OkHttpClient], [Retrofit] ve API arayüzleri.
 *
 * Backend `https://streaming-api.halitkalayci.com` üzerinde sunulur (bkz. docs/api/openapi.json
 * servers[0]). Karar geçmişi için bkz. docs/decisions.md — Ağ Katmanı.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://streaming-api.halitkalayci.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore = TokenStore(context)

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStore: TokenStore): AuthInterceptor = AuthInterceptor(tokenStore)

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideSongsApi(retrofit: Retrofit): SongsApi = retrofit.create(SongsApi::class.java)

    @Provides
    @Singleton
    fun providePlaylistsApi(retrofit: Retrofit): PlaylistsApi = retrofit.create(PlaylistsApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMeApi(retrofit: Retrofit): MeApi = retrofit.create(MeApi::class.java)
}
