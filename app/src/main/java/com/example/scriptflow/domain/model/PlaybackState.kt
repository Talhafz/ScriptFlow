package com.example.scriptflow.domain.model

sealed class PlaybackState {
    object Idle : PlaybackState()
    data class Countdown(val secondsLeft: Int) : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Finished : PlaybackState()
}
