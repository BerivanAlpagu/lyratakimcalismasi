package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.library.DefaultLibraryRepository
import com.turkcell.lyraapp.data.library.LibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [LibraryRepository] arayüzünü somut implementasyonuna ([DefaultLibraryRepository]) bağlar.
 *
 * `@Binds` ile yapıldığından Hilt fazladan nesne üretmez; gerçek API implementasyonu
 * değiştirildiğinde yalnızca buradaki bağlama hedefi güncellenir, ViewModel/Contract
 * etkilenmez.
 *
 * Karar geçmişi için bkz. docs/decisions.md — Library Ekranı.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: DefaultLibraryRepository): LibraryRepository
}
