package com.example.scriptflow.feature.settings.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.model.TextAlignment

@Composable
fun LivePreviewCard(
    settings: TeleprompterSettings,
    modifier: Modifier = Modifier
) {
    // Animate offset to show speed change
    val infiniteTransition = rememberInfiniteTransition(label = "preview")
    
    // We base the animation duration on the scroll speed multiplier
    // Higher speed = shorter duration for one loop
    val baseDuration = 4000 // 4 seconds at 1.0x
    val duration = (baseDuration / settings.scrollSpeed).toInt()

    val offset by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(settings.backgroundColor)),
            contentAlignment = Alignment.Center
        ) {
            // Reading Zone Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Text(
                text = "The quick brown fox jumps over the lazy dog. ScriptFlow is now faster and more responsive.",
                color = Color(settings.textColor),
                fontSize = (settings.fontSize / 2).sp, 
                lineHeight = (settings.fontSize / 2 * settings.lineSpacing).sp,
                letterSpacing = settings.letterSpacing.sp,
                textAlign = when (settings.textAlignment) {
                    TextAlignment.LEFT -> TextAlign.Left
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.Right
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(24.dp)
                    .graphicsLayer {
                        translationY = offset
                        if (settings.mirrorMode) {
                            scaleX = -1f
                        }
                    }
            )
        }
    }
}
