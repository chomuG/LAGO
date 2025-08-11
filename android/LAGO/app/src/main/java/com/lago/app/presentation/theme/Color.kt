package com.lago.app.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Custom App Colors
val MainBlue = Color(0xFF42A6FF)
val MainPink = Color(0xFFFF99C5)
val AppBackground = Color(0xFFF7FAFD)
val ShadowColor = Color(0xFF738CD7).copy(alpha = 0.15f)
val Black = Color(0xFF1E1E20)

// Gray Scale
val Gray100 = Color(0xFFF7F8F9)
val Gray200 = Color(0xFFE8EAED)
val Gray300 = Color(0xFFDADCE0)
val Gray400 = Color(0xFFBDC1C6)
val Gray500 = Color(0xFF9AA0A6)
val Gray600 = Color(0xFF80868B)
val Gray700 = Color(0xFF5F6368)
val Gray800 = Color(0xFF3C4043)
val Gray900 = Color(0xFF202124)

// Blue Scale
val BlueLight = Color(0xFFECF6FF)
val BlueLightHover = Color(0xFFE3F2FF)
val BlueLightActive = Color(0xFFC4E3FF)
val BlueNormal = Color(0xFF42A6FF)
val BlueNormalHover = Color(0xFF3B95E6)
val BlueNormalActive = Color(0xFF3585CC)
val BlueDark = Color(0xFF327DBF)
val BlueDarkHover = Color(0xFF286499)
val BlueDarkActive = Color(0xFF1E4B73)
val BlueDarker = Color(0xFF173A59)

val LightColorScheme = lightColorScheme(
    primary = MainBlue,
    secondary = MainPink,
    tertiary = BlueLightActive,
    background = AppBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BlueDark,
    onBackground = Gray900,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700
)

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color(0xFF371E73),
    onSecondary = Color(0xFF332D41),
    onTertiary = Color(0xFF492532),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)