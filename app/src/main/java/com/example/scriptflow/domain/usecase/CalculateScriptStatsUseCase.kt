package com.example.scriptflow.domain.usecase

import javax.inject.Inject

class CalculateScriptStatsUseCase @Inject constructor() {
    operator fun invoke(content: String, wpm: Int): ScriptStats {
        val words = content.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size
        val charCount = content.length
        
        // Estimated duration in seconds
        val durationSeconds = if (wpm > 0) (wordCount.toDouble() / wpm * 60).toInt() else 0
        
        return ScriptStats(
            wordCount = wordCount,
            charCount = charCount,
            durationSeconds = durationSeconds
        )
    }

    data class ScriptStats(
        val wordCount: Int,
        val charCount: Int,
        val durationSeconds: Int
    )
}
