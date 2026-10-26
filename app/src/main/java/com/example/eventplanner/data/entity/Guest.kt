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
    val rsvpStatus: String = "Pending",   // Pending, Accepted, Declined
    val inviteToken: String = "",         // secret per-guest token for the RSVP link
    val partySize: Int = 1,               // how many people the guest is bringing in total
    val note: String = "",                // optional message from the guest
    val respondedAt: Long = 0             // time of the guest's reply (0 = no reply yet)
)