package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.profile.FakeProfileRepository
import com.turkcell.lyraapp.data.profile.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        fakeRepository: FakeProfileRepository
    ): ProfileRepository
}
