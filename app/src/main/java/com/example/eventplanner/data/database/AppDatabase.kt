package com.example.eventplanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.eventplanner.data.dao.ChecklistDao
import com.example.eventplanner.data.dao.EventDao
import com.example.eventplanner.data.dao.ExpenseDao
import com.example.eventplanner.data.dao.GuestDao
import com.example.eventplanner.data.entity.ChecklistItem
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.data.entity.Expense
import com.example.eventplanner.data.entity.Guest

@Database(
    entities = [Event::class, Guest::class, ChecklistItem::class, Expense::class],
    version = 5,   // ← check: (your current version) + 1
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun guestDao(): GuestDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Keeps all existing data: only adds columns and a new table
        private val MIGRATION_ADD_INVITES_AND_EXPENSES = object : Migration(4, 5) {   // ← check: (current, current + 1)
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE guests ADD COLUMN inviteToken TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE guests ADD COLUMN partySize INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE guests ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE guests ADD COLUMN respondedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS expenses (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "eventId INTEGER NOT NULL, " +
                            "title TEXT NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "amount REAL NOT NULL, " +
                            "isPaid INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eventplanner_database"
                )
                    .addMigrations(MIGRATION_ADD_INVITES_AND_EXPENSES)
                    .fallbackToDestructiveMigration()   // safety net for very old installs only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}