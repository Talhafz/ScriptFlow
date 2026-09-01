package com.example.scriptflow.feature.home

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.scriptflow.data.local.database.ScriptDatabase
import com.example.scriptflow.data.repository.ScriptRepositoryImpl
import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.usecase.DeleteScriptUseCase
import com.example.scriptflow.domain.usecase.GetScriptsUseCase
import com.example.scriptflow.domain.usecase.SaveScriptUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HomeViewModelTest {

    private lateinit var database: ScriptDatabase
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ScriptDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Seed data
        val script1 = Script(title = "Script A", content = "Content A", createdAt = 1, updatedAt = 1, category = "Videos")
        val script2 = Script(title = "Script B", content = "Content B", createdAt = 2, updatedAt = 2, category = "Speeches")
        val script3 = Script(title = "Script C", content = "Content C", createdAt = 3, updatedAt = 3, category = "Videos")
        
        database.scriptDao().insertScript(com.example.scriptflow.data.local.entity.ScriptEntity.fromDomain(script1))
        database.scriptDao().insertScript(com.example.scriptflow.data.local.entity.ScriptEntity.fromDomain(script2))
        database.scriptDao().insertScript(com.example.scriptflow.data.local.entity.ScriptEntity.fromDomain(script3))

        val repository = ScriptRepositoryImpl(database.scriptDao())
        val getScriptsUseCase = GetScriptsUseCase(repository)
        val deleteScriptUseCase = DeleteScriptUseCase(repository)
        val saveScriptUseCase = SaveScriptUseCase(repository)
        
        viewModel = HomeViewModel(getScriptsUseCase, deleteScriptUseCase, saveScriptUseCase)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `category filtering narrows the list correctly`() = runTest {
        // Wait for flow to collect initial data
        advanceUntilIdle()
        
        // 2. Filter by "Videos"
        viewModel.onCategorySelected("Videos")
        advanceUntilIdle()
        
        val stateVideos = viewModel.uiState.value as HomeUiState.Success
        assert(stateVideos.scripts.size == 2) { "Expected 2 scripts for 'Videos', found ${stateVideos.scripts.size}" }
        assert(stateVideos.scripts.all { it.category == "Videos" })
        
        // 3. Filter by "Speeches"
        viewModel.onCategorySelected("Speeches")
        advanceUntilIdle()
        
        val stateSpeeches = viewModel.uiState.value as HomeUiState.Success
        assert(stateSpeeches.scripts.size == 1)
        assert(stateSpeeches.scripts[0].title == "Script B")
        
        // 4. Filter by "All" (clear state)
        viewModel.onCategorySelected("All")
        advanceUntilIdle()
        
        val stateAll = viewModel.uiState.value as HomeUiState.Success
        assert(stateAll.scripts.size == 3)
    }
}
