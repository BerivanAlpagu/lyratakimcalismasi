package com.turkcell.lyraapp.data.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
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
}
