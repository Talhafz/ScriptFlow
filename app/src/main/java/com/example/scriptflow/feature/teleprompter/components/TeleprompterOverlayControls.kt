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

@Composable
fun TeleprompterOverlayControls(
    isVisible: Boolean,
    isQuickSettingsVisible: Boolean,
    playbackState: PlaybackState,
    settings: TeleprompterSettings,
    onPlayPauseClick: () -> Unit,
    onRestartClick: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onWpmChange: (Int) -> Unit,
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
                                Text("Speed (WPM)", color = Color.White)
                                Text("${settings.wpm} WPM", color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = settings.wpm.toFloat(),
                                onValueChange = { onWpmChange(it.toInt()) },
                                valueRange = 80f..250f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                            )
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
                                contentDescription = "Scroll speed ${settings.wpm} WPM"
                            }
                        ) {
                            IconButton(onClick = { onWpmChange(settings.wpm - 10) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease speed", tint = Color.White)
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${settings.wpm}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "wpm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            
                            IconButton(onClick = { onWpmChange(settings.wpm + 10) }) {
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
                
                Spacer(Modifier.height(24.dp))
                
                // Progress Bar Placeholder
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f) // Placeholder progress
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "2:20",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
