package com.example.scriptflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.scriptflow.data.local.dao.ScriptDao
import com.example.scriptflow.data.local.entity.ScriptEntity

@Database(entities = [ScriptEntity::class], version = 2)
abstract class ScriptDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scripts ADD COLUMN category TEXT")
            }
        }
    }
}
