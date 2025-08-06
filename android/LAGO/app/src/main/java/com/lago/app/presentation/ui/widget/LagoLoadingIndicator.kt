package com.lago.app.presentation.ui.widget

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lago.app.presentation.theme.*

@Composable
fun LagoLoadingIndicator(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false
) {
    if (isFullScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            LoadingContent()
        }
    } else {
        LoadingContent(modifier)
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .size(100.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MainBlue.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
        
        // Main loading indicator
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = MainBlue,
            strokeWidth = 3.dp
        )
        
        // Inner circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MainPink.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun LagoSkeletonLoader(
    modifier: Modifier = Modifier,
    height: Int = 100
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .alpha(alpha)
            .background(
                color = Gray200,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    )
}