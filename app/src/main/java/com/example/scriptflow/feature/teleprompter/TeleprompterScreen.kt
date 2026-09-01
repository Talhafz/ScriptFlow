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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptflow.domain.model.PlaybackState
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
                    TeleprompterContent(
                        scriptContent = uiState.script?.content ?: "",
                        settings = uiState.settings,
                        scrollOffsetProvider = { scrollOffset },
                        onTextLayoutMeasured = viewModel::onTextLayoutMeasured
                    )
                }
                
                ReadingZone()

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
                    onWpmChange = viewModel::updateWpm,
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
    onTextLayoutMeasured: (Float) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (settings.mirrorMode) {
                    scaleX = -1f
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = -scrollOffsetProvider()
                }
                .onGloballyPositioned { layoutCoordinates ->
                    onTextLayoutMeasured(layoutCoordinates.size.height.toFloat())
                }
                .padding(horizontal = 48.dp)
        ) {
            Spacer(modifier = Modifier.height(screenHeight / 2))
            
            // To achieve the yellow text in the reading zone, we would ideally use a custom layout
            // or a shader. For now, we'll use the primary color if we want the "Live" feel.
            // The image shows the text *at* the reading zone is yellow.
            
            Text(
                text = scriptContent,
                color = Color.White, // Default color, ideally shadowed or masked
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = settings.fontSize.sp,
                    lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                    letterSpacing = settings.letterSpacing.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = when (settings.textAlignment) {
                    com.example.scriptflow.domain.model.TextAlignment.LEFT -> TextAlign.Start
                    com.example.scriptflow.domain.model.TextAlignment.CENTER -> TextAlign.Center
                    com.example.scriptflow.domain.model.TextAlignment.RIGHT -> TextAlign.End
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(screenHeight))
        }
    }
}

@Composable
fun ReadingZone() {
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
