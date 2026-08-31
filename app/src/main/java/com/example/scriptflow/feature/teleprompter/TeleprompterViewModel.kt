package com.example.scriptflow.feature.teleprompter

import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    
    private var textWidth: Float = 0f
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
                        _scrollOffset.value = script.lastPosition.toFloat()
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

    fun onTextLayoutMeasured(width: Float) {
        if (width > 0 && textWidth != width) {
            textWidth = width
            calculateSpeed()
        }
    }

    private fun calculateSpeed() {
        val script = _uiState.value.script ?: return
        val settings = _uiState.value.settings
        
        if (textWidth <= 0) return

        val words = script.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size.coerceAtLeast(1)
        val durationSeconds = if (settings.wpm > 0) (words.toDouble() / settings.wpm * 60) else 1.0
        
        pixelsPerSecond = (textWidth / durationSeconds.toFloat()) * settings.scrollSpeed
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
                
                // Stop if we scrolled past the end
                if (newOffset > textWidth + 100) {
                    pausePlayback()
                    _uiState.value = _uiState.value.copy(playbackState = PlaybackState.Finished)
                }
            }
        }
    }

    fun toggleControls() {
        if (_uiState.value.areControlsVisible) {
            _uiState.value = _uiState.value.copy(areControlsVisible = false)
            controlsTimerJob?.cancel()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        _uiState.value = _uiState.value.copy(areControlsVisible = true)
        if (_uiState.value.playbackState is PlaybackState.Playing) {
            hideControlsWithDelay()
        }
    }

    private fun hideControlsWithDelay() {
        controlsTimerJob?.cancel()
        controlsTimerJob = viewModelScope.launch {
            delay(3000)
            _uiState.value = _uiState.value.copy(areControlsVisible = false)
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
