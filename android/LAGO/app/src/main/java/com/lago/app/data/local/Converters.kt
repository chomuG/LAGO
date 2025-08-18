package com.lago.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lago.app.domain.entity.CandlestickData
import java.text.SimpleDateFormat
import java.util.*

class Converters {
    
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    @TypeConverter
    fun fromDate(date: Date?): String? {
        return date?.let { formatter.format(it) }
    }

    @TypeConverter
    fun toDate(dateString: String?): Date? {
        return dateString?.let {
            try {
                formatter.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Chart data converters
    @TypeConverter
    fun fromCandlestickDataList(value: List<CandlestickData>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCandlestickDataList(value: String): List<CandlestickData> {
        val listType = object : TypeToken<List<CandlestickData>>() {}.type
        return Gson().fromJson(value, listType)
    }
}