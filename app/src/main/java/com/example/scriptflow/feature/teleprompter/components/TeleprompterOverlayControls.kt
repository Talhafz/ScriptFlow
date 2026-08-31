package com.example.scriptflow.feature.teleprompter.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.scriptflow.domain.model.PlaybackState
import com.example.scriptflow.domain.model.TeleprompterSettings

@Composable
fun TeleprompterOverlayControls(
    isVisible: Boolean,
    playbackState: PlaybackState,
    settings: TeleprompterSettings,
    onPlayPauseClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onExitClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                }
                
                IconButton(
                    onClick = onSettingsClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            // Central Controls
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = onRestartClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Scroll speed ${String.format("%.1fx", settings.scrollSpeed)}"
                        }
                    ) {
                        IconButton(onClick = { onSpeedChange(settings.scrollSpeed - 0.1f) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease speed")
                        }
                        Text(
                            text = String.format("%.1fx", settings.scrollSpeed),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = { onSpeedChange(settings.scrollSpeed + 0.1f) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase speed")
                        }
                    }

                    FloatingActionButton(
                        onClick = onPlayPauseClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = if (playbackState is PlaybackState.Playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState is PlaybackState.Playing) "Pause" else "Play"
                        )
                    }
                    
                    Text(
                        text = "${settings.wpm} WPM",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}
