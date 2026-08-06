package com.presencial.app.di

import android.content.Context
import androidx.room.Room
import com.presencial.app.data.local.PresencialDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.data.preferences.SettingsDataStore
import com.presencial.app.data.repository.AbsenceRepositoryImpl
import com.presencial.app.data.repository.CheckInRepositoryImpl
import com.presencial.app.data.repository.MonthlySummaryRepositoryImpl
import com.presencial.app.data.repository.WorkAddressRepositoryImpl
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.util.DefaultTimeProvider
import com.presencial.app.domain.util.TimeProvider
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

    @Provides
    fun provideAbsenceDao(db: PresencialDatabase): AbsenceDao = db.absenceDao()

    @Provides
    fun provideWorkAddressDao(db: PresencialDatabase): WorkAddressDao = db.workAddressDao()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
}

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindTimeProvider(impl: DefaultTimeProvider): TimeProvider

    @Binds
    @Singleton
    fun bindCheckInRepository(impl: CheckInRepositoryImpl): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindMonthlySummaryRepository(impl: MonthlySummaryRepositoryImpl): MonthlySummaryRepository

    @Binds
    @Singleton
    abstract fun bindAbsenceRepository(impl: AbsenceRepositoryImpl): AbsenceRepository

    @Binds
    @Singleton
    abstract fun bindWorkAddressRepository(impl: WorkAddressRepositoryImpl): WorkAddressRepository
}
