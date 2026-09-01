package com.example.scriptflow.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ScriptDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert some data
            execSQL("INSERT INTO scripts (title, content, createdAt, updatedAt, lastPosition) VALUES ('Test Script', 'Test Content', 12345, 67890, 0)")
            close()
        }

        // Open latest version of the database with migration object.
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, ScriptDatabase.MIGRATION_1_2)

        // Verify that the data exists and has the new column
        val cursor = db.query("SELECT * FROM scripts")
        assert(cursor.moveToFirst())
        
        val titleIndex = cursor.getColumnIndex("title")
        val contentIndex = cursor.getColumnIndex("content")
        val categoryIndex = cursor.getColumnIndex("category")
        
        assert(cursor.getString(titleIndex) == "Test Script")
        assert(cursor.getString(contentIndex) == "Test Content")
        assert(cursor.isNull(categoryIndex)) // Should be null for existing rows
        
        cursor.close()
    }
}
