package com.presencial.app.di

import com.presencial.app.data.location.AndroidGeofenceRegistrar
import com.presencial.app.domain.location.GeofenceRegistrar
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeofenceModule {
    @Binds
    @Singleton
    abstract fun bindGeofenceRegistrar(impl: AndroidGeofenceRegistrar): GeofenceRegistrar
}
