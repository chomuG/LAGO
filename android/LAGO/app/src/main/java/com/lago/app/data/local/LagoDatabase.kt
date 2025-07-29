package com.lago.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

import com.lago.app.domain.entity.User

@Database(
    entities = [
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LagoDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    
    companion object {
        const val DATABASE_NAME = "lago_database"
    }
}