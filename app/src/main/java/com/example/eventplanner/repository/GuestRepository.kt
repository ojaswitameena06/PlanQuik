package com.example.eventplanner.repository

import com.example.eventplanner.data.dao.GuestDao
import com.example.eventplanner.data.entity.Guest
import kotlinx.coroutines.flow.Flow

class GuestRepository(private val guestDao: GuestDao) {

    fun getGuestsForEvent(eventId: Long): Flow<List<Guest>> {
        return guestDao.getGuestsForEvent(eventId)
    }

    suspend fun insertGuest(guest: Guest): Long {
        return guestDao.insertGuest(guest)
    }

    suspend fun updateGuest(guest: Guest) {
        guestDao.updateGuest(guest)
    }

    suspend fun deleteGuest(guest: Guest) {
        guestDao.deleteGuest(guest)
    }
}