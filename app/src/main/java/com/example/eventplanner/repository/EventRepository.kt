package com.example.eventplanner.repository

import com.example.eventplanner.data.dao.EventDao
import com.example.eventplanner.data.entity.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    fun getEventsForUser(userId: String): Flow<List<Event>> = eventDao.getEventsForUser(userId)

    suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun getEventById(eventId: Long): Event? {
        return eventDao.getEventById(eventId)
    }
}