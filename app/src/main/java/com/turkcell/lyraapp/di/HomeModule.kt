package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.home.DefaultHomeRepository
import com.turkcell.lyraapp.data.home.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Home feature'ının repository bağlamaları.
 *
 * [HomeRepository], "Şarkılar" bölümünü gerçek API'dan çeken [DefaultHomeRepository]'ye bağlanır
 * (diğer bölümler hâlâ statiktir). Tüm bölümler API'ya taşındığında yalnızca bu bağlamanın
 * hedefi değişir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: DefaultHomeRepository): HomeRepository
}
