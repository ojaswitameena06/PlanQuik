package com.example.eventplanner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM guests WHERE eventId = :eventId")
    suspend fun getGuestsForEventOnce(eventId: Long): List<Guest>

    @Query("SELECT * FROM guests WHERE inviteToken != ''")
    fun getGuestsWithTokens(): Flow<List<Guest>>

    @Query("SELECT * FROM guests WHERE inviteToken = :token LIMIT 1")
    suspend fun getGuestByToken(token: String): Guest?

    @Query(
        "UPDATE guests SET rsvpStatus = :status, partySize = :partySize, " +
                "note = :note, respondedAt = :respondedAt WHERE inviteToken = :token"
    )
    suspend fun applyRsvp(token: String, status: String, partySize: Int, note: String, respondedAt: Long)
}