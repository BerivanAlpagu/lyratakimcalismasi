package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.player.DefaultPlayerRepository
import com.turkcell.lyraapp.data.player.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [PlayerRepository] bağlamasını sağlar (bkz. di/HomeModule.kt deseni).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: DefaultPlayerRepository): PlayerRepository
}
