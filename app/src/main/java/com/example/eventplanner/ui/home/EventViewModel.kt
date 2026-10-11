package com.example.eventplanner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.database.AppDatabase
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.repository.EventRepository
import com.example.eventplanner.sync.FirebaseSyncHelper
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    val allEvents: LiveData<List<Event>>

    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        allEvents = repository.getEventsForUser(uid).asLiveData()
    }

    fun insertEvent(event: Event) = viewModelScope.launch {
        val id = repository.insertEvent(event)
        FirebaseSyncHelper.pushEvent(event.copy(id = id))
    }

    fun updateEvent(event: Event) = viewModelScope.launch {
        repository.updateEvent(event)
        FirebaseSyncHelper.pushEvent(event)
    }

    fun deleteEvent(event: Event) = viewModelScope.launch {
        repository.deleteEvent(event)
        FirebaseSyncHelper.deleteEvent(event.id)
    }

    suspend fun getEventById(eventId: Long) = repository.getEventById(eventId)
}