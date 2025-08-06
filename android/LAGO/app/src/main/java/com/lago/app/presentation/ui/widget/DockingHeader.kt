package com.lago.app.presentation.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import com.lago.app.presentation.theme.*
import com.lago.app.domain.entity.StockInfo

@OptIn(ExperimentalMotionApi::class)
@Composable
fun DockingHeader(
    stockInfo: StockInfo,
    progress: Float, // 0f(peek) → 1f(expanded)
    modifier: Modifier = Modifier
) {
    val startConstraintSet = ConstraintSet {
        val titleRef = createRefFor("title")
        val priceRef = createRefFor("price")
        val changeRef = createRefFor("change")
        val percentRef = createRefFor("percent")
        
        constrain(titleRef) {
            top.linkTo(parent.top, margin = 120.dp)
            start.linkTo(parent.start, margin = 16.dp)
        }
        constrain(priceRef) {
            top.linkTo(titleRef.bottom, margin = 8.dp)
            start.linkTo(parent.start, margin = 16.dp)
        }
        constrain(changeRef) {
            top.linkTo(parent.top, margin = 120.dp)
            end.linkTo(parent.end, margin = 16.dp)
        }
        constrain(percentRef) {
            top.linkTo(changeRef.bottom, margin = 4.dp)
            end.linkTo(parent.end, margin = 16.dp)
        }
    }
    
    val endConstraintSet = ConstraintSet {
        val titleRef = createRefFor("title")
        val priceRef = createRefFor("price")
        val changeRef = createRefFor("change")
        val percentRef = createRefFor("percent")
        
        constrain(titleRef) {
            top.linkTo(parent.top, margin = 16.dp)
            start.linkTo(parent.start, margin = 56.dp)
        }
        constrain(priceRef) {
            top.linkTo(parent.top, margin = 16.dp)
            start.linkTo(titleRef.end, margin = 8.dp)
        }
        constrain(changeRef) {
            top.linkTo(parent.top, margin = 16.dp)
            start.linkTo(priceRef.end, margin = 4.dp)
        }
        constrain(percentRef) {
            top.linkTo(parent.top, margin = 16.dp)
            start.linkTo(changeRef.end, margin = 4.dp)
        }
    }
    
    MotionLayout(
        start = startConstraintSet,
        end = endConstraintSet,
        progress = progress.coerceIn(0f, 1f),
        modifier = modifier.fillMaxWidth()
    ) {
        val titleScale = 1f - (progress * 0.33f) // 1f -> 0.67f
        val priceScale = 1f - (progress * 0.5f)  // 1f -> 0.5f
        val changeScale = 1f - (progress * 0.2f) // 1f -> 0.8f
        
        val isPositive = stockInfo.priceChange >= 0
        val changeColor = if (isPositive) MainPink else MainBlue
        
        // 종목명
        Text(
            text = stockInfo.name,
            modifier = Modifier
                .layoutId("title")
                .graphicsLayer {
                    scaleX = titleScale
                    scaleY = titleScale
                },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = (24f - progress * 6f).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Gray900
        )
        
        // 현재가
        Text(
            text = "${String.format("%.0f", stockInfo.currentPrice)}원",
            modifier = Modifier
                .layoutId("price")
                .graphicsLayer {
                    scaleX = priceScale
                    scaleY = priceScale
                },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = (32f - progress * 16f).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Gray900
        )
        
        // 변동금액
        Text(
            text = if (isPositive) {
                "+${String.format("%.0f", stockInfo.priceChange)}원"
            } else {
                "${String.format("%.0f", stockInfo.priceChange)}원"
            },
            modifier = Modifier
                .layoutId("change")
                .graphicsLayer {
                    scaleX = changeScale
                    scaleY = changeScale
                },
            style = TitleB16.copy(
                fontSize = (16f - progress * 2f).sp
            ),
            color = changeColor
        )
        
        // 변동률
        Text(
            text = "(${if (isPositive) "+" else ""}${String.format("%.2f", stockInfo.priceChangePercent)}%)",
            modifier = Modifier
                .layoutId("percent")
                .graphicsLayer {
                    scaleX = changeScale
                    scaleY = changeScale
                },
            style = SubtitleSb14.copy(
                fontSize = (14f - progress * 0f).sp
            ),
            color = changeColor
        )
    }
}