package com.presencial.app.di

import android.content.Context
import androidx.room.Room
import com.presencial.app.data.local.PresencialDatabase
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.preferences.SettingsDataStore
import com.presencial.app.data.repository.CheckInRepositoryImpl
import com.presencial.app.data.repository.MonthlySummaryRepositoryImpl
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import dagger.Binds
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
    fun provideDatabase(@ApplicationContext context: Context): PresencialDatabase =
        Room.databaseBuilder(context, PresencialDatabase::class.java, "presencial.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCheckInDao(db: PresencialDatabase): CheckInDao = db.checkInDao()

    @Provides
    fun provideMonthlySummaryDao(db: PresencialDatabase): MonthlySummaryDao = db.monthlySummaryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(impl: CheckInRepositoryImpl): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindMonthlySummaryRepository(impl: MonthlySummaryRepositoryImpl): MonthlySummaryRepository
}
