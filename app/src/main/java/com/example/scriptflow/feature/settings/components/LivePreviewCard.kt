package com.example.scriptflow.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(settings.backgroundColor)),
            contentAlignment = Alignment.Center
        ) {
            // Reading Zone Indicator for Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(settings.textColor).copy(alpha = 0.1f))
            )

            Text(
                text = "Photosynthesis is the process by which green plants make their own food.",
                color = Color(settings.textColor),
                fontSize = (settings.fontSize / 2).sp, // Scaled down for preview
                lineHeight = (settings.fontSize / 2 * settings.lineSpacing).sp,
                letterSpacing = settings.letterSpacing.sp,
                textAlign = when (settings.textAlignment) {
                    TextAlignment.LEFT -> TextAlign.Left
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.Right
                },
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(16.dp)
                    .graphicsLayer {
                        if (settings.mirrorMode) {
                            scaleX = -1f
                        }
                    }
            )
        }
    }
}
