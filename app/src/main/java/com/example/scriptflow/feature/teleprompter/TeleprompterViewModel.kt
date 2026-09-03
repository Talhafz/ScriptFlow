package com.example.scriptflow.feature.teleprompter

import android.util.Log
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptflow.domain.model.DisplayMode
import com.example.scriptflow.domain.model.PlaybackState
import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.usecase.GetScriptsUseCase
import com.example.scriptflow.domain.usecase.GetSettingsUseCase
import com.example.scriptflow.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeleprompterViewModel @Inject constructor(
    private val getScriptsUseCase: GetScriptsUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scriptId: Long = savedStateHandle.get<Long>("scriptId") ?: -1L

    private val _uiState = MutableStateFlow(TeleprompterUiState())
    val uiState: StateFlow<TeleprompterUiState> = _uiState.asStateFlow()

    private val _scrollOffset = MutableStateFlow(0f)
    val scrollOffset: StateFlow<Float> = _scrollOffset.asStateFlow()

    private var tickerJob: Job? = null
    private var controlsTimerJob: Job? = null
    
    private var scrollDimension: Float = 0f
    private var pixelsPerSecond: Float = 0f

    init {
        loadData()
        observeSettings()
    }

    private fun loadData() {
        viewModelScope.launch {
            if (scriptId != -1L) {
                // Observe script to handle background deletions
                getScriptsUseCase().collectLatest { scripts ->
                    val script = scripts.find { it.id == scriptId }
                    if (script != null) {
                        _uiState.value = _uiState.value.copy(
                            script = script,
                            isLoading = false
                        )
                        _scrollOffset.value = 0f // FORCE RESET
                        Log.d("TeleprompterVM", "Script loaded: ${script.title}, length: ${script.content.length}")
                        calculateSpeed()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Script not found (ID: $scriptId)"
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid Script ID"
                )
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
                calculateSpeed()
            }
        }
    }

    fun onTextLayoutMeasured(dimension: Float) {
        if (dimension > 0 && scrollDimension != dimension) {
            scrollDimension = dimension
            calculateSpeed()
        }
    }

    private fun calculateSpeed() {
        val settings = _uiState.value.settings
        
        if (scrollDimension <= 0) return

        // New Aggressive Speed Model:
        // Speed is independent of word count and instead scales with font size.
        // Base velocity (1.0x) is now 8x the font size per second.
        val baseVelocity = settings.fontSize * 8f
        
        pixelsPerSecond = baseVelocity * settings.scrollSpeed
    }

    fun togglePlayback() {
        when (_uiState.value.playbackState) {
            is PlaybackState.Playing -> pausePlayback()
            is PlaybackState.Paused, is PlaybackState.Idle -> startCountdown()
            else -> Unit
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            var seconds = _uiState.value.settings.countdownSeconds
            while (seconds > 0) {
                _uiState.value = _uiState.value.copy(playbackState = PlaybackState.Countdown(seconds))
                delay(1000)
                seconds--
            }
            startPlayback()
        }
    }

    private fun startPlayback() {
        _uiState.value = _uiState.value.copy(playbackState = PlaybackState.Playing)
        hideControlsWithDelay()
    }

    private fun pausePlayback() {
        _uiState.value = _uiState.value.copy(playbackState = PlaybackState.Paused)
        showControls()
    }

    fun restartPlayback() {
        _scrollOffset.value = 0f
        _uiState.value = _uiState.value.copy(
            playbackState = PlaybackState.Idle
        )
        showControls()
    }

    fun updateScrollOffset(deltaSeconds: Float) {
        if (_uiState.value.playbackState is PlaybackState.Playing) {
            if (pixelsPerSecond.isFinite() && pixelsPerSecond > 0) {
                val newOffset = _scrollOffset.value + (pixelsPerSecond * deltaSeconds)
                _scrollOffset.value = newOffset
                
                // NEW LIMIT LOGIC:
                // With coordinate anchoring, the text is fully scrolled past the top 
                // when offset > contentHeight + (screenHeight / 2)
                // We use a safe buffer of 2000 for horizontal marquee as before.
                val limit = if (_uiState.value.settings.displayMode == DisplayMode.HORIZONTAL) {
                    scrollDimension + 2000
                } else {
                    scrollDimension + 1000 // Safely clears the reading zone
                }
                
                if (newOffset > limit) {
                    pausePlayback()
                    _uiState.value = _uiState.value.copy(playbackState = PlaybackState.Finished)
                }
            }
        }
    }

    fun onManualScroll(delta: Float) {
        _scrollOffset.value = (_scrollOffset.value + delta).coerceAtLeast(0f)
        showControls()
    }

    fun toggleControls() {
        if (_uiState.value.areControlsVisible) {
            _uiState.value = _uiState.value.copy(
                areControlsVisible = false,
                isQuickSettingsVisible = false
            )
            controlsTimerJob?.cancel()
        } else {
            showControls()
        }
    }

    fun showControls() {
        _uiState.value = _uiState.value.copy(areControlsVisible = true)
        if (_uiState.value.playbackState is PlaybackState.Playing && !_uiState.value.isQuickSettingsVisible) {
            hideControlsWithDelay()
        }
    }

    fun toggleQuickSettings() {
        val nextVisible = !_uiState.value.isQuickSettingsVisible
        _uiState.value = _uiState.value.copy(isQuickSettingsVisible = nextVisible)
        if (nextVisible) {
            controlsTimerJob?.cancel() // Keep visible while adjusting
        } else if (_uiState.value.playbackState is PlaybackState.Playing) {
            hideControlsWithDelay()
        }
    }

    private fun hideControlsWithDelay() {
        controlsTimerJob?.cancel()
        controlsTimerJob = viewModelScope.launch {
            delay(2000) 
            if (!_uiState.value.isQuickSettingsVisible) {
                _uiState.value = _uiState.value.copy(areControlsVisible = false)
            }
        }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(fontSize = size.coerceIn(20f, 72f))
            updateSettingsUseCase(newSettings)
        }
    }

    fun updateWpm(wpm: Int) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(wpm = wpm.coerceIn(80, 250))
            updateSettingsUseCase(newSettings)
        }
    }

    fun updateDisplayMode(mode: DisplayMode) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(displayMode = mode)
            updateSettingsUseCase(newSettings)
        }
    }

    fun updateSpeed(multiplier: Float) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings
            val newSettings = currentSettings.copy(scrollSpeed = multiplier)
            updateSettingsUseCase(newSettings)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        controlsTimerJob?.cancel()
    }
}
