package com.lago.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

import com.lago.app.domain.entity.User
import com.lago.app.data.local.entity.CachedChartDataEntity
import com.lago.app.data.local.entity.CachedStockInfoEntity
import com.lago.app.data.local.dao.ChartCacheDao

@Database(
    entities = [
        User::class,
        CachedChartDataEntity::class,
        CachedStockInfoEntity::class
    ],
    version = 2,
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