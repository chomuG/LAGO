package com.lago.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lago.app.R

val PretendardFamily = FontFamily(
    Font(R.font.pretendard_thin, FontWeight.Thin),
    Font(R.font.pretendard_extralight, FontWeight.ExtraLight),
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
    Font(R.font.pretendard_black, FontWeight.Black)
)

// Custom Typography Styles
val HeadEb32 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 32.sp
)

val TitleB28 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp
)

val SubtitleSb28 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp
)

val BodyR24 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp
)

val HeadEb28 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp
)

val TitleB24 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp
)

val SubtitleSb24 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp
)

val BodyR20 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
)

val TitleB20 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp
)

val SubtitleSb20 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp
)

val BodyR18 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp
)

val HeadEb24 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp
)

val TitleB18 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp
)

val SubtitleSb18 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp
)

val BodyR16 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)

val TitleB16 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp
)

val SubtitleSb16 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp
)

val BodyR14 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)

val HeadEb20 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 20.sp
)

val TitleB14 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp
)

val BodyR12 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
)

val HeadEb18 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 18.sp
)

val SubtitleSb14 = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp
)

val Typography = Typography(
    // Display
    displayLarge = HeadEb32,
    displayMedium = HeadEb28,
    displaySmall = HeadEb24,
    
    // Headline
    headlineLarge = TitleB28,
    headlineMedium = TitleB24,
    headlineSmall = TitleB20,
    
    // Title
    titleLarge = TitleB20,
    titleMedium = TitleB18,
    titleSmall = TitleB16,
    
    // Body
    bodyLarge = BodyR16.copy(lineHeight = 24.sp),
    bodyMedium = BodyR14.copy(lineHeight = 20.sp),
    bodySmall = BodyR12.copy(lineHeight = 16.sp),
    
    // Label
    labelLarge = SubtitleSb14,
    labelMedium = SubtitleSb14.copy(fontSize = 12.sp),
    labelSmall = BodyR12.copy(fontWeight = FontWeight.Medium)
)