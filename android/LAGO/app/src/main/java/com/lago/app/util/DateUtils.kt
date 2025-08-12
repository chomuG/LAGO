package com.lago.app.util

import java.text.SimpleDateFormat
import java.util.*

fun formatTimeAgo(publishedAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        
        val publishedTime = inputFormat.parse(publishedAt) ?: return publishedAt
        val now = Date()
        val diffMillis = now.time - publishedTime.time
        
        val diffDays = diffMillis / (24 * 60 * 60 * 1000)
        val diffHours = diffMillis / (60 * 60 * 1000)
        val diffMinutes = diffMillis / (60 * 1000)
        
        when {
            diffDays > 7 -> outputFormat.format(publishedTime)
            diffDays > 0 -> "${diffDays}일 전"
            diffHours > 0 -> "${diffHours}시간 전" 
            diffMinutes > 0 -> "${diffMinutes}분 전"
            else -> "방금 전"
        }
    } catch (e: Exception) {
        publishedAt
    }
}