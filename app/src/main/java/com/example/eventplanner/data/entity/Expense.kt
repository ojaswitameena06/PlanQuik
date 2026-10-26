package com.example.eventplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val title: String,
    val category: String,      // Venue, Food, Decor, Gifts, Other
    val amount: Double,
    val isPaid: Boolean = false
)