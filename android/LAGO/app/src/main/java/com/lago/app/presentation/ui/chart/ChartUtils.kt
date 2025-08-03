package com.lago.app.presentation.ui.chart

import com.tradingview.lightweightcharts.api.series.models.Time
import java.text.SimpleDateFormat
import java.util.*

object ChartUtils {
    
    fun timestampToBusinessDay(timestamp: Long): Time {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        return Time.BusinessDay(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1, // Calendar.MONTH는 0부터 시작
            day = calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    
    fun timestampToDateString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        
        return "$year-$month-$day"
    }
    
    fun generateDateSequence(count: Int = 30): List<Long> {
        val calendar = Calendar.getInstance()
        val timestamps = mutableListOf<Long>()
        
        // 오늘부터 count일 전까지의 날짜 생성
        for (i in count - 1 downTo 0) {
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_MONTH, -i)
            timestamps.add(tempCalendar.timeInMillis)
        }
        
        return timestamps
    }
}