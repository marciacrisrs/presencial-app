package com.presencial.app.di

import android.content.Context
import com.presencial.app.data.local.PresencialDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.dao.BackupDao
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
import com.presencial.app.data.location.AndroidGeocodingHelper
import com.presencial.app.domain.location.GeocodingHelper
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.util.DefaultTimeProvider
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.data.holidays.RegionalHolidayCatalog
import com.presencial.app.data.local.AndroidSmartMessageTextProvider
import com.presencial.app.data.sync.CloudFolderSyncProvider
import com.presencial.app.data.sync.CloudSyncRepositoryImpl
import com.presencial.app.domain.repository.CloudSyncRepository
import com.presencial.app.domain.sync.CloudSyncProvider
import com.presencial.app.domain.util.RegionalHolidayLookup
import com.presencial.app.domain.util.SmartMessageTextProvider
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.widget.AndroidWidgetRefresher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PresencialDatabase =
        PresencialDatabase.getInstance(context)

    @Provides
    fun provideCheckInDao(db: PresencialDatabase): CheckInDao = db.checkInDao()

    @Provides
    fun provideMonthlySummaryDao(db: PresencialDatabase): MonthlySummaryDao = db.monthlySummaryDao()

    @Provides
    fun provideAbsenceDao(db: PresencialDatabase): AbsenceDao = db.absenceDao()

    @Provides
    fun provideWorkAddressDao(db: PresencialDatabase): WorkAddressDao = db.workAddressDao()

    @Provides
    fun provideBackupDao(db: PresencialDatabase): BackupDao = db.backupDao()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "presencial_settings")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindTimeProvider(impl: DefaultTimeProvider): TimeProvider

    @Binds
    @Singleton
    fun bindCheckInRepository(impl: CheckInRepositoryImpl): CheckInRepository

    @Binds
    @Singleton
    fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository

    @Binds
    @Singleton
    fun bindMonthlySummaryRepository(impl: MonthlySummaryRepositoryImpl): MonthlySummaryRepository

    @Binds
    @Singleton
    fun bindAbsenceRepository(impl: AbsenceRepositoryImpl): AbsenceRepository

    @Binds
    @Singleton
    fun bindWorkAddressRepository(impl: WorkAddressRepositoryImpl): WorkAddressRepository

    @Binds
    @Singleton
    fun bindGeocodingHelper(impl: AndroidGeocodingHelper): GeocodingHelper

    @Binds
    @Singleton
    fun bindWidgetRefresher(impl: AndroidWidgetRefresher): WidgetRefresher

    @Binds
    @Singleton
    fun bindSmartMessageTextProvider(impl: AndroidSmartMessageTextProvider): SmartMessageTextProvider

    @Binds
    @Singleton
    fun bindRegionalHolidayLookup(impl: RegionalHolidayCatalog): RegionalHolidayLookup

    @Binds
    @Singleton
    fun bindCloudSyncProvider(impl: CloudFolderSyncProvider): CloudSyncProvider

    @Binds
    @Singleton
    fun bindCloudSyncRepository(impl: CloudSyncRepositoryImpl): CloudSyncRepository
}
