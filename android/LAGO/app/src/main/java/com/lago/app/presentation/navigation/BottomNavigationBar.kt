package com.lago.app.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lago.app.presentation.theme.MainBlue

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
}

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        // 강제로 라이트 테마 적용하여 흰색 배경 보장
        MaterialTheme(
            colorScheme = lightColorScheme(
                surface = Color.White,
                surfaceContainer = Color.White,
                surfaceContainerLow = Color.White,
                surfaceContainerLowest = Color.White
            )
        ) {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.background(Color.White)
            ) {
                NavigationBar(
                    modifier = Modifier
                        .height(114.dp)
                        .background(Color.White),
                    containerColor = Color.White
                ) {
        bottomNavigationItems.forEach { item ->
            val isSelected = currentRoute == item.route
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "scale_animation"
            )
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier.scale(scale).offset(y = 2.dp)

                    )
                },
                label = {
                    Text(
                        text = item.title,
                        modifier = Modifier.scale(scale)
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MainBlue,
                    selectedTextColor = MainBlue,
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                    indicatorColor = Color.Transparent
                ),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = if (currentRoute == item.route) {
                        "${item.title} 탭, 현재 선택됨"
                    } else {
                        "${item.title} 탭으로 이동"
                    }
                }
            )
        }
                }
            }
        }
    }
}