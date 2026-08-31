package com.example.scriptflow.feature.teleprompter.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CountdownOverlay(secondsLeft: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = secondsLeft,
            transitionSpec = {
                (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
            },
            label = "CountdownAnimation"
        ) { targetSeconds ->
            Text(
                text = if (targetSeconds > 0) targetSeconds.toString() else "START!",
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
