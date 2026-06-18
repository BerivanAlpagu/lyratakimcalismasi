package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.library.DefaultLibraryRepository
import com.turkcell.lyraapp.data.library.LibraryRepository
import com.turkcell.lyraapp.data.playlists.PlaylistsApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Kütüphane (Library) feature'ı için Hilt bağımlılık modülü.
 *
 * İki sorumluluk:
 *  1. [PlaylistsApi] Retrofit instance'ından üretilir ([providePlaylistsApi]).
 *     `NetworkModule.kt`'ye dokunulmaz; böylece diğer takım üyelerinin branch'leriyle
 *     merge çakışması sıfırlanır (bkz. implementation_plan.md — Açık Sorular).
 *  2. [LibraryRepository] → [DefaultLibraryRepository] bağlaması (@Binds).
 *     Gerçek API'ya geçildiğinde yalnızca @Binds hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(
        impl: DefaultLibraryRepository,
    ): LibraryRepository

    companion object {

        /**
         * [PlaylistsApi] Retrofit instance'ından üretilir.
         *
         * [Retrofit] tek-ton olarak [com.turkcell.lyraapp.data.network.NetworkModule]
         * tarafından sağlanır; burada yalnızca tüketilir, değiştirilmez.
         */
        @Provides
        @Singleton
        fun providePlaylistsApi(retrofit: Retrofit): PlaylistsApi =
            retrofit.create(PlaylistsApi::class.java)
    }
}
