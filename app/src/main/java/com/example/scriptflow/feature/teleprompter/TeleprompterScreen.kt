package com.example.scriptflow.feature.teleprompter

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.scriptflow.domain.model.TextAlignment
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
            // Give user time to see the error if they are already on screen
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
        
        // Handle Orientation
        if (uiState.settings.orientation == com.example.scriptflow.domain.model.ScreenOrientation.LANDSCAPE) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        
        // Handle Keep Screen Awake
        if (uiState.settings.keepScreenAwake) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        // Fullscreen / Immersive
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
            .background(Color(uiState.settings.backgroundColor))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.toggleControls()
            }
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                Text(
                    text = "No content to display",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                TeleprompterContent(
                    scriptContent = uiState.script?.content ?: "",
                    settings = uiState.settings,
                    scrollOffsetProvider = { scrollOffset },
                    onTextLayoutMeasured = viewModel::onTextLayoutMeasured
                )
            }
                
                // Reading Zone Overlay
                ReadingZone(uiState.settings.textColor)

                // Overlays
                (uiState.playbackState as? PlaybackState.Countdown)?.let { countdown ->
                    CountdownOverlay(secondsLeft = countdown.secondsLeft)
                }

                TeleprompterOverlayControls(
                    isVisible = uiState.areControlsVisible,
                    playbackState = uiState.playbackState,
                    settings = uiState.settings,
                    onPlayPauseClick = viewModel::togglePlayback,
                    onRestartClick = viewModel::restartPlayback,
                    onSpeedChange = viewModel::updateSpeed,
                    onSettingsClick = { /* Navigate to Settings or show local settings */ },
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
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (settings.mirrorMode) {
                    scaleX = -1f
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(unbounded = true, align = Alignment.Start)
                .graphicsLayer {
                    translationX = -scrollOffsetProvider()
                }
                .onGloballyPositioned { layoutCoordinates ->
                    onTextLayoutMeasured(layoutCoordinates.size.width.toFloat())
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start Padding to start text at the reading zone (Center of screen)
            Spacer(modifier = Modifier.width(screenWidth / 2))
            
            Text(
                text = scriptContent.replace("\n", " "), // Ensure single line
                color = Color(settings.textColor),
                fontSize = settings.fontSize.sp,
                lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                letterSpacing = settings.letterSpacing.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Black,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.wrapContentWidth()
            )
            
            // End Padding to allow scrolling past the end
            Spacer(modifier = Modifier.width(screenWidth))
        }
    }
}

@Composable
fun ReadingZone(textColor: Long) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val zoneWidth = 200.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = (screenWidth / 2) - (zoneWidth / 2))
    ) {
        // Vertical guides for horizontal scrolling
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color(textColor).copy(alpha = 0.3f))
                .align(Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color(textColor).copy(alpha = 0.3f))
                .align(Alignment.CenterEnd)
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
