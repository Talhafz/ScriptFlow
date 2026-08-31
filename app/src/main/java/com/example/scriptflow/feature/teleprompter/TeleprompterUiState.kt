package com.example.scriptflow.feature.teleprompter

import com.example.scriptflow.domain.model.PlaybackState
import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.model.TeleprompterSettings

data class TeleprompterUiState(
    val script: Script? = null,
    val settings: TeleprompterSettings = TeleprompterSettings(),
    val playbackState: PlaybackState = PlaybackState.Idle,
    val areControlsVisible: Boolean = true,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
