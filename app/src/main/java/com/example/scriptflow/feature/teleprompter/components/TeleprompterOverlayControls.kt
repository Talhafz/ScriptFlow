package com.example.scriptflow.feature.teleprompter.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scriptflow.domain.model.PlaybackState
import com.example.scriptflow.domain.model.TeleprompterSettings
import java.util.Locale

@Composable
fun TeleprompterOverlayControls(
    isVisible: Boolean,
    isQuickSettingsVisible: Boolean,
    playbackState: PlaybackState,
    settings: TeleprompterSettings,
    onPlayPauseClick: () -> Unit,
    onRestartClick: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onDisplayModeChange: (com.example.scriptflow.domain.model.DisplayMode) -> Unit,
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExitClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Text(
                    text = "Teleprompter", 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Tune, 
                        contentDescription = "Settings", 
                        tint = if (isQuickSettingsVisible) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            // Quick Settings Overlay
            if (isQuickSettingsVisible) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 400.dp)
                        .padding(bottom = 80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            "QUICK SETTINGS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Font Size", color = Color.White)
                                Text("${settings.fontSize.toInt()} sp", color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = settings.fontSize,
                                onValueChange = onFontSizeChange,
                                valueRange = 20f..72f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Scroll Speed", color = Color.White)
                                Text(String.format(Locale.getDefault(), "%.1fx", settings.scrollSpeed), color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = settings.scrollSpeed,
                                onValueChange = onSpeedChange,
                                valueRange = 0.5f..5.0f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }

                        // Display Mode Toggle
                        Column {
                            Text("Display Mode", color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                com.example.scriptflow.domain.model.DisplayMode.entries.forEachIndexed { index, mode ->
                                    SegmentedButton(
                                        selected = settings.displayMode == mode,
                                        onClick = { onDisplayModeChange(mode) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = com.example.scriptflow.domain.model.DisplayMode.entries.size),
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.primary,
                                            activeContentColor = Color.Black,
                                            inactiveContainerColor = Color.Transparent,
                                            inactiveContentColor = Color.White
                                        )
                                    ) {
                                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Central/Bottom Floating Controls
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Font Size Toggle Standalone
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.size(40.dp),
                            onClick = { 
                                val newSize = if (settings.fontSize >= 72f) 20f else settings.fontSize + 4f
                                onFontSizeChange(newSize)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("A", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("A", fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
                            }
                        }

                        // Speed Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics(mergeDescendants = true) {
                                contentDescription = "Scroll speed ${settings.scrollSpeed}x"
                            }
                        ) {
                            IconButton(onClick = { onSpeedChange((settings.scrollSpeed - 0.1f).coerceAtLeast(0.5f)) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease speed", tint = Color.White)
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1fx", settings.scrollSpeed),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "speed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            
                            IconButton(onClick = { onSpeedChange((settings.scrollSpeed + 0.1f).coerceAtMost(5.0f)) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase speed", tint = Color.White)
                            }
                        }

                        // Play/Pause Button (Yellow Circle)
                        Surface(
                            onClick = onPlayPauseClick,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (playbackState is PlaybackState.Playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState is PlaybackState.Playing) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
