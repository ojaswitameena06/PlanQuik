package com.example.eventplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guests")
data class Guest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val name: String,
    val phone: String,
    val email: String = "",
    val rsvpStatus: String = "Pending" // Pending, Accepted, Declined
)