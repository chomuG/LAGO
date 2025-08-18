package com.lago.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

import com.lago.app.domain.entity.User
import com.lago.app.data.local.entity.CachedChartData
import com.lago.app.data.local.entity.CachedStockInfo
import com.lago.app.data.local.dao.ChartCacheDao

@Database(
    entities = [
        User::class,
        CachedChartData::class,
        CachedStockInfo::class
    ],
    version = 2, // 버전 증가
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LagoDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun chartCacheDao(): ChartCacheDao
    
    companion object {
        const val DATABASE_NAME = "lago_database"
    }
}