package com.lago.app.presentation.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lago.app.R
import com.lago.app.domain.entity.News
import com.lago.app.presentation.theme.*
import com.lago.app.util.formatTimeAgo

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewsCard(
    news: News,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = ShadowColor,
                ambientColor = ShadowColor
            )
            .height(128.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = when (news.sentiment) {
                                "호재" -> Color(0xFFFFE9F2)
                                "악재" -> BlueLightHover
                                else -> Gray100
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = news.sentiment,
                        style = TitleB14,
                        color = when (news.sentiment) {
                            "호재" -> Color(0xFFFF6DAC)
                            "악재" -> BlueNormalHover
                            else -> Gray600
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title
                Text(
                    text = news.title,
                    style = SubtitleSb14,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time
                Text(
                    text = formatTimeAgo(news.publishedAt),
                    style = BodyR12,
                    color = Gray700
                )
            }
            
            // News Image
            Image(
                painter = painterResource(id = R.drawable.megaphone_image),
                contentDescription = "뉴스 이미지",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
            )
        }
    }
}