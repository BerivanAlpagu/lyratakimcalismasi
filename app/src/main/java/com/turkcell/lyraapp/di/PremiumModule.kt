package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.premium.DefaultPremiumRepository
import com.turkcell.lyraapp.data.premium.PremiumApi
import com.turkcell.lyraapp.data.premium.PremiumRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumModule {

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        defaultPremiumRepository: DefaultPremiumRepository
    ): PremiumRepository

    companion object {
        @Provides
        @Singleton
        fun providePremiumApi(retrofit: Retrofit): PremiumApi {
            return retrofit.create(PremiumApi::class.java)
        }
    }
}
