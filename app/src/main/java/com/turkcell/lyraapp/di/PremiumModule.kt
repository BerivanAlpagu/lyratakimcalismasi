package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.premium.DefaultPremiumRepository
import com.turkcell.lyraapp.data.premium.PremiumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumModule {

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        impl: DefaultPremiumRepository,
    ): PremiumRepository
}
