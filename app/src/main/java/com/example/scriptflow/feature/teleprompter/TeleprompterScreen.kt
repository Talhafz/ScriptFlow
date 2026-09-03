package com.example.scriptflow.feature.teleprompter

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.scriptflow.feature.editor.splitIntoChunks
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptflow.domain.model.DisplayMode
import com.example.scriptflow.domain.model.PlaybackState
import com.example.scriptflow.feature.editor.parseMarkdown
import com.example.scriptflow.feature.teleprompter.components.CountdownOverlay
import com.example.scriptflow.feature.teleprompter.components.TeleprompterOverlayControls
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TeleprompterScreen(
    onBack: () -> Unit,
    viewModel: TeleprompterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollOffset by viewModel.scrollOffset.collectAsState()
    val context = LocalContext.current

    // Playback Ticker
    LaunchedEffect(uiState.playbackState) {
        if (uiState.playbackState is PlaybackState.Playing) {
            var lastFrameTime = System.nanoTime()
            while (true) {
                withFrameNanos { frameTime ->
                    val deltaSeconds = (frameTime - lastFrameTime) / 1_000_000_000f
                    lastFrameTime = frameTime
                    viewModel.updateScrollOffset(deltaSeconds)
                }
            }
        }
    }

    // Handle background deletion
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            if (uiState.isLoading.not()) {
                delay(2000.milliseconds)
                onBack()
            }
        }
    }
    
    // Immersive and Orientation Control
    DisposableEffect(uiState.settings.orientation, uiState.settings.keepScreenAwake) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        
        if (uiState.settings.orientation == com.example.scriptflow.domain.model.ScreenOrientation.LANDSCAPE) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        
        if (uiState.settings.keepScreenAwake) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            activity.requestedOrientation = originalOrientation
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.toggleControls() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { viewModel.showControls() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        viewModel.onManualScroll(-dragAmount.y)
                    }
                )
            }
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                if (uiState.script?.content.isNullOrBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No content to display",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    if (uiState.settings.displayMode == DisplayMode.HORIZONTAL) {
                        HorizontalMarqueeContent(
                            scriptContent = uiState.script?.content ?: "",
                            settings = uiState.settings,
                            scrollOffsetProvider = { scrollOffset },
                            onWidthMeasured = viewModel::onTextLayoutMeasured
                        )
                    } else {
                        TeleprompterContent(
                            scriptContent = uiState.script?.content ?: "",
                            settings = uiState.settings,
                            scrollOffsetProvider = { scrollOffset },
                            onHeightMeasured = viewModel::onTextLayoutMeasured
                        )
                    }
                }
                
                ReadingZone(displayMode = uiState.settings.displayMode)

                (uiState.playbackState as? PlaybackState.Countdown)?.let { countdown ->
                    CountdownOverlay(secondsLeft = countdown.secondsLeft)
                }

                TeleprompterOverlayControls(
                    isVisible = uiState.areControlsVisible,
                    isQuickSettingsVisible = uiState.isQuickSettingsVisible,
                    playbackState = uiState.playbackState,
                    settings = uiState.settings,
                    onPlayPauseClick = viewModel::togglePlayback,
                    onRestartClick = viewModel::restartPlayback,
                    onFontSizeChange = viewModel::updateFontSize,
                    onSpeedChange = viewModel::updateSpeed,
                    onDisplayModeChange = viewModel::updateDisplayMode,
                    onSettingsClick = viewModel::toggleQuickSettings,
                    onExitClick = onBack
                )
            }
        }
    }
}

@Composable
fun TeleprompterContent(
    scriptContent: String,
    settings: com.example.scriptflow.domain.model.TeleprompterSettings,
    scrollOffsetProvider: () -> Float,
    onHeightMeasured: (Float) -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (settings.mirrorMode) {
                    scaleX = -1f
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        val totalHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val centerOffset = totalHeightPx / 2f
        
        // Calculate half of the first line height to perfectly center it
        val halfLineHeightPx = with(LocalDensity.current) { 
            ((settings.fontSize * settings.lineSpacing) / 2).sp.toPx() 
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                .graphicsLayer {
                    // Precision coordinate anchoring
                    translationY = centerOffset - halfLineHeightPx - scrollOffsetProvider()
                }
                .onGloballyPositioned { layoutCoordinates ->
                    onHeightMeasured(layoutCoordinates.size.height.toFloat())
                }
                .padding(horizontal = if (settings.fontSize > 40) 24.dp else 48.dp)
        ) {
            // Handle Alignment segments
            val blocks = scriptContent.splitByAlignment(settings.textAlignment)
            
            blocks.forEach { block ->
                val lines = block.text.split("\n")
                lines.forEach { line ->
                    if (line.isNotEmpty()) {
                        val chunks = line.splitIntoChunks(maxChars = 400)
                        chunks.forEach { chunk ->
                            Text(
                                text = chunk.parseMarkdown(accentColor = accentColor),
                                color = Color.White,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = settings.fontSize.sp,
                                    lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                                    letterSpacing = settings.letterSpacing.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = block.alignment,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            )
                        }
                    } else {
                        // Empty line
                        Spacer(modifier = Modifier.height((settings.fontSize / 2).dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalMarqueeContent(
    scriptContent: String,
    settings: com.example.scriptflow.domain.model.TeleprompterSettings,
    scrollOffsetProvider: () -> Float,
    onWidthMeasured: (Float) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Use the bold, blocky transit-style font (Anton)
    // Red color and increased size for high impact
    val displayStyle = MaterialTheme.typography.displayMedium.copy(
        fontFamily = com.example.scriptflow.ui.theme.AntonFontFamily,
        fontSize = (settings.fontSize * 2.2f).sp, // Increased to fill vertical band
        lineHeight = (settings.fontSize * 2.2f * 1.2f).sp, // Fix clipping by ensuring enough line height
        fontWeight = FontWeight.Black,
        letterSpacing = 4.sp,
        color = Color(0xFFFF2A2A) // vivid red
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (settings.mirrorMode) {
                    scaleX = -1f
                }
            },
        contentAlignment = Alignment.CenterStart // Centers text vertically
    ) {
        val density = LocalDensity.current
        Row(
            modifier = Modifier
                .wrapContentWidth(unbounded = true)
                .wrapContentHeight(unbounded = true) // Allow text to grow beyond standard bounds
                .graphicsLayer {
                    // Start from the beginning (right edge of screen)
                    val startX = with(density) { screenWidth.toPx() }
                    translationX = startX - scrollOffsetProvider()
                }
                .onGloballyPositioned { layoutCoordinates ->
                    onWidthMeasured(layoutCoordinates.size.width.toFloat())
                }
        ) {
            // Horizontal prompter strips newlines and processes as a single long line in ALL CAPS
            val cleanContent = scriptContent.replace("\n", " ").trim().uppercase()
            Text(
                text = cleanContent,
                style = displayStyle,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

data class AlignmentBlock(val text: String, val alignment: TextAlign)

fun String.splitByAlignment(defaultAlignment: com.example.scriptflow.domain.model.TextAlignment): List<AlignmentBlock> {
    val result = mutableListOf<AlignmentBlock>()
    val regex = Regex("\\[ALIGN:(LEFT|CENTER|RIGHT)\\]")
    var lastIndex = 0
    var currentAlignment = when (defaultAlignment) {
        com.example.scriptflow.domain.model.TextAlignment.LEFT -> TextAlign.Start
        com.example.scriptflow.domain.model.TextAlignment.CENTER -> TextAlign.Center
        com.example.scriptflow.domain.model.TextAlignment.RIGHT -> TextAlign.End
    }
    
    val matches = regex.findAll(this).toList()
    
    if (matches.isEmpty()) {
        result.add(AlignmentBlock(this, currentAlignment))
        return result
    }

    matches.forEach { match ->
        val textBefore = this.substring(lastIndex, match.range.first)
        if (textBefore.isNotEmpty()) {
            result.add(AlignmentBlock(textBefore, currentAlignment))
        }
        currentAlignment = when (match.groupValues[1]) {
            "LEFT" -> TextAlign.Start
            "CENTER" -> TextAlign.Center
            "RIGHT" -> TextAlign.End
            else -> currentAlignment
        }
        lastIndex = match.range.last + 1
    }
    
    val remaining = this.substring(lastIndex)
    if (remaining.isNotEmpty()) {
        result.add(AlignmentBlock(remaining, currentAlignment))
    }
    
    return result
}

@Composable
fun ReadingZone(displayMode: DisplayMode) {
    if (displayMode != DisplayMode.VERTICAL) return

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val zoneHeight = 80.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = (screenHeight / 2) - (zoneHeight / 2))
    ) {
        // High-vis yellow guides from image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.BottomCenter)
        )
        
        // Active indicator arrow on the left
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
