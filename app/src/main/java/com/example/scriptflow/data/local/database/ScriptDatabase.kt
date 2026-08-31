package com.example.scriptflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.scriptflow.data.local.dao.ScriptDao
import com.example.scriptflow.data.local.entity.ScriptEntity

@Database(entities = [ScriptEntity::class], version = 1)
abstract class ScriptDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
}
