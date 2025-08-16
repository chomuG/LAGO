package com.lago.app.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

fun formatTimeAgo(publishedAt: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        formatTimeAgoModern(publishedAt)
    } else {
        formatTimeAgoLegacy(publishedAt)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimeAgoModern(publishedAt: String): String {
    return try {
        val now = LocalDateTime.now()
        
        // 다양한 타임스탬프 포맷 지원
        val publishedTime = when {
            publishedAt.contains("T") && publishedAt.contains("Z") -> {
                LocalDateTime.parse(publishedAt.replace("Z", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
            }
            publishedAt.contains("T") -> {
                LocalDateTime.parse(publishedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            }
            publishedAt.contains(" ") -> {
                LocalDateTime.parse(publishedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            else -> {
                LocalDateTime.parse(publishedAt + "T00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            }
        }
        
        val diffMinutes = ChronoUnit.MINUTES.between(publishedTime, now)
        val diffHours = ChronoUnit.HOURS.between(publishedTime, now)
        val diffDays = ChronoUnit.DAYS.between(publishedTime, now)
        
        when {
            diffDays > 7 -> publishedTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            diffDays > 0 -> "${diffDays}일 전"
            diffHours > 0 -> "${diffHours}시간 전"
            diffMinutes > 0 -> "${diffMinutes}분 전"
            else -> "방금 전"
        }
    } catch (e: Exception) {
        android.util.Log.w("DateUtils", "Failed to parse timestamp: $publishedAt", e)
        publishedAt
    }
}

private fun formatTimeAgoLegacy(publishedAt: String): String {
    return try {
        // 다양한 포맷 시도
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        
        var publishedTime: Date? = null
        for (pattern in formats) {
            try {
                val format = SimpleDateFormat(pattern, Locale.getDefault())
                publishedTime = format.parse(publishedAt.replace("Z", ""))
                if (publishedTime != null) break
            } catch (e: Exception) {
                continue
            }
        }
        
        if (publishedTime == null) return publishedAt
        
        val now = Date()
        val diffMillis = now.time - publishedTime.time
        
        val diffDays = diffMillis / (24 * 60 * 60 * 1000)
        val diffHours = diffMillis / (60 * 60 * 1000)
        val diffMinutes = diffMillis / (60 * 1000)
        
        when {
            diffDays > 7 -> {
                val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                outputFormat.format(publishedTime)
            }
            diffDays > 0 -> "${diffDays}일 전"
            diffHours > 0 -> "${diffHours}시간 전"
            diffMinutes > 0 -> "${diffMinutes}분 전"
            else -> "방금 전"
        }
    } catch (e: Exception) {
        android.util.Log.w("DateUtils", "Failed to parse timestamp: $publishedAt", e)
        publishedAt
    }
}