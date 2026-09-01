package com.example.scriptflow.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MigrationTest {

    @Test
    fun migrate1To2() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration-test-db"
        context.deleteDatabase(dbName)

        // 1. Create v1 database using plain SQLite
        val v1Db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        v1Db.version = 1 // Set Room version to 1
        v1Db.execSQL("CREATE TABLE IF NOT EXISTS `scripts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `lastPosition` INTEGER NOT NULL)")
        v1Db.execSQL("INSERT INTO scripts (title, content, createdAt, updatedAt, lastPosition) VALUES ('Test Script', 'Test Content', 12345, 67890, 0)")
        v1Db.close()

        // 2. Open latest version of the database with Room and the migration
        val db = Room.databaseBuilder(context, ScriptDatabase::class.java, dbName)
            .addMigrations(ScriptDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        // 3. Verify that the data exists and has the new column (defaulting to null)
        val script = db.scriptDao().getScriptById(1)
        
        assert(script != null)
        assert(script?.title == "Test Script")
        assert(script?.content == "Test Content")
        assert(script?.category == null) // The core of the test
        
        db.close()
    }
}
