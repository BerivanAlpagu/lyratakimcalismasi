package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.favorites.FakeFavoritesRepository
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesModule {

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        fakeRepository: FakeFavoritesRepository
    ): FavoritesRepository
}
