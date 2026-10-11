package com.example.eventplanner.data.dao

import androidx.room.*
import com.example.eventplanner.data.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY date ASC, time ASC")
    fun getEventsForUser(userId: String): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): Event?
}