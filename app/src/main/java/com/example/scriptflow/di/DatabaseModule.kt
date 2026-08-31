package com.example.scriptflow.di

import android.content.Context
import androidx.room.Room
import com.example.scriptflow.data.local.database.ScriptDatabase
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
    fun provideScriptDatabase(
        @ApplicationContext context: Context
    ): ScriptDatabase {
        return Room.databaseBuilder(
            context,
            ScriptDatabase::class.java,
            "script_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScriptDao(db: ScriptDatabase) = db.scriptDao()
}
