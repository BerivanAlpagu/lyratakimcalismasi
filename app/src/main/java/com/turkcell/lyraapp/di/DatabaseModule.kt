package com.turkcell.lyraapp.di

import android.content.Context
import androidx.room.Room
import com.turkcell.lyraapp.data.local.DownloadedSongDao
import com.turkcell.lyraapp.data.local.LyraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLyraDatabase(
        @ApplicationContext context: Context
    ): LyraDatabase {
        return Room.databaseBuilder(
            context,
            LyraDatabase::class.java,
            "lyra_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDownloadedSongDao(database: LyraDatabase): DownloadedSongDao {
        return database.downloadedSongDao()
    }
}
