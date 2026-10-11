package com.example.eventplanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.eventplanner.data.dao.ChecklistDao
import com.example.eventplanner.data.dao.EventDao
import com.example.eventplanner.data.dao.GuestDao
import com.example.eventplanner.data.entity.ChecklistItem
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.data.entity.Guest

@Database(entities = [Event::class, Guest::class, ChecklistItem::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun guestDao(): GuestDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eventplanner_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}