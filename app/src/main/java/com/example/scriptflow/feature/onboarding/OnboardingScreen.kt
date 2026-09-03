package com.example.scriptflow.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptflow.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPage(
            title = "ScriptFlow",
            subtitle = "Your words. On point. Every time.",
            description = "The premium teleprompter for creators who demand perfection.",
            icon = null // Special handling for logo
        ),
        OnboardingPage(
            title = "Smooth Auto-Scroll",
            subtitle = "Perfect pacing, automatically.",
            description = "Adjust speed on the fly or let our smart-scroll track your voice.",
            icon = Icons.Default.Bolt
        ),
        OnboardingPage(
            title = "Fully Customizable",
            subtitle = "Designed for your eyes.",
            description = "Adjust fonts, colors, and margins for maximum readability in any light.",
            icon = Icons.Default.TextFields
        ),
        OnboardingPage(
            title = "Camera Ready",
            subtitle = "Focus where it matters.",
            description = "Overlay your script directly over your camera for perfect eye contact.",
            icon = Icons.Default.CameraAlt
        )
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "Skip",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable {
                        viewModel.completeOnboarding(onGetStarted)
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(
                    page = pages[pageIndex],
                    isFirstPage = pageIndex == 0
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager Indicator
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else Color.White.copy(alpha = 0.2f)
                                )
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (pagerState.currentPage == 3) {
                            viewModel.completeOnboarding(onGetStarted)
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = if (pagerState.currentPage == 3) "Get Started" else "Next",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (pagerState.currentPage < 3) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isFirstPage: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration / Logo Area
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFirstPage) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo_scriptflow),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White)) { append("Script") }
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Flow") }
                        },
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                // "Premium" Custom Illustration construction
                Box(contentAlignment = Alignment.Center) {
                    // Background Glow/Circle
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                CircleShape
                            )
                    )
                    // Icon with subtle depth
                    Icon(
                        imageVector = page.icon!!,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    // Decorative element
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = 4.dp, y = 4.dp)
                            .alpha(0.1f),
                        tint = Color.White
                    )
                }
            }
        }

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector?
)
