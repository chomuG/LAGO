package com.lago.app.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimeAgo(publishedAt: String): String {
    return try {
        val publishedTime = Instant.parse(publishedAt)
        val now = Instant.now()
        val duration = java.time.Duration.between(publishedTime, now)
        
        when {
            duration.toDays() > 7 -> {
                val zonedDateTime = ZonedDateTime.ofInstant(publishedTime, ZoneId.systemDefault())
                zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
            duration.toDays() > 0 -> "${duration.toDays()}일 전"
            duration.toHours() > 0 -> "${duration.toHours()}시간 전"
            duration.toMinutes() > 0 -> "${duration.toMinutes()}분 전"
            else -> "방금 전"
        }
    } catch (e: Exception) {
        publishedAt
    }
}