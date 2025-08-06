package com.lago.app.presentation.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
// Icon imports removed - no longer using triangle icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lago.app.domain.entity.StockInfo
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.theme.MainPink
import com.lago.app.presentation.theme.MainBlue
// abs import removed - no longer needed

@Composable
fun AnimatedHeaderBox(
    stockInfo: StockInfo,
    headerAlignmentProgress: Float, // 0f to 1f (30% ~ 40% 범위에서 0f to 1f로 변환됨)
    contentOffsetY: Float,
    modifier: Modifier = Modifier
) {
    // 박스 애니메이션 값들 계산
    val boxPadding = 16f - (headerAlignmentProgress * 16f) // 16 -> 0
    val boxTranslationY = -headerAlignmentProgress * 32f // 0 -> -32 (앱바 아이콘과 같은 높이)
    val boxHeight = 120f - (headerAlignmentProgress * 64f) // 120 -> 56
    val boxAlpha = 0f // 항상 투명
    val boxCornerRadius = 12f - (headerAlignmentProgress * 12f) // 12 -> 0

    // 텍스트 애니메이션 값들
    val titleScale = 1f - (headerAlignmentProgress * 0.25f) // 1f -> 0.75f
    val priceScale = 1f - (headerAlignmentProgress * 0.4f) // 1f -> 0.6f
    val layoutTransition = headerAlignmentProgress // 레이아웃 전환 진행도
    
    // Theme Typography 사용 - 정의된 스타일 사용
    
    // 부드러운 전환을 위한 easing 함수
    val easeInOut = { progress: Float ->
        if (progress < 0.5f) {
            2f * progress * progress
        } else {
            1f - 2f * (1f - progress) * (1f - progress)
        }
    }
    val easedTransition = easeInOut(layoutTransition)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = boxPadding.dp)
            .offset(y = (72f + boxTranslationY + contentOffsetY).dp)
            .height(boxHeight.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = boxAlpha),
                shape = RoundedCornerShape(boxCornerRadius.dp)
            )
            .zIndex(2f)
    ) {
        // 박스 안의 콘텐츠 - 부드러운 fade in/out
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 초기 상태 (세로 배치) - 부드럽게 사라짐
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.md)
                    .alpha((1f - easedTransition).coerceAtLeast(0f))
                    .graphicsLayer {
                        translationY = -easedTransition * 10f // 부드러운 이동
                        scaleX = titleScale
                        scaleY = titleScale
                    },
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stockInfo.name,
                    style = SubtitleSb24,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // 가격과 수익률을 한 줄에 배치
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.0f", stockInfo.currentPrice)}원",
                        style = HeadEb32,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(Spacing.sm + Spacing.xs))
                    
                    val isPositive = stockInfo.priceChange >= 0
                    val changeText = if (isPositive) {
                        "+${String.format("%.0f", stockInfo.priceChange)}원 (${String.format("%.2f", stockInfo.priceChangePercent)}%)"
                    } else {
                        "${String.format("%.0f", stockInfo.priceChange)}원 (${String.format("%.2f", stockInfo.priceChangePercent)}%)"
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    
                    Text(
                        text = changeText,
                        style = SubtitleSb14,
                        color = if (isPositive) MainPink else MainBlue
                    )
                }
            }

            // 최종 상태 (앱바 안 세로 배치) - 뒤로가기 버튼 바로 오른쪽에 위치
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = Spacing.xxxl - Spacing.md) // 뒤로가기 버튼 + 여백
                    .alpha(easedTransition.coerceAtLeast(0f))
                    .graphicsLayer {
                        translationY = (1f - easedTransition) * 10f // 부드러운 이동
                    }
            ) {
                // 주가 타이틀
                Text(
                    text = stockInfo.name,
                    style = SubtitleSb16,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 금액과 수익률을 한 줄에
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.0f", stockInfo.currentPrice)}원",
                        style = BodyR14,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(Spacing.xs + 2.dp))

                    val isPositive = stockInfo.priceChange >= 0
                    val percentText = if (isPositive) {
                        "(${String.format("%.2f", stockInfo.priceChangePercent)}%)"
                    } else {
                        "(${String.format("%.2f", stockInfo.priceChangePercent)}%)"
                    }
                    
                    Text(
                        text = percentText,
                        style = SubtitleSb14,
                        color = if (isPositive) MainPink else MainBlue
                    )
                }
            }
        }
    }
}