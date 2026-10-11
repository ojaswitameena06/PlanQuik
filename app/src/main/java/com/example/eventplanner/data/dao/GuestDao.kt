package com.example.eventplanner.data.dao

import androidx.room.*
import com.example.eventplanner.data.entity.Guest
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestDao {

    @Insert
    suspend fun insertGuest(guest: Guest): Long

    @Update
    suspend fun updateGuest(guest: Guest)

    @Delete
    suspend fun deleteGuest(guest: Guest)

    @Query("SELECT * FROM guests WHERE eventId = :eventId")
    fun getGuestsForEvent(eventId: Long): Flow<List<Guest>>
}