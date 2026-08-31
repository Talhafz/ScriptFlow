package com.example.scriptflow.di

import com.example.scriptflow.data.repository.ScriptRepositoryImpl
import com.example.scriptflow.data.repository.SettingsRepositoryImpl
import com.example.scriptflow.domain.repository.ScriptRepository
import com.example.scriptflow.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScriptRepository(
        scriptRepositoryImpl: ScriptRepositoryImpl
    ): ScriptRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
