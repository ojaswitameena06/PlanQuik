package com.example.eventplanner.ui.guests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.database.AppDatabase
import com.example.eventplanner.data.entity.Guest
import com.example.eventplanner.repository.GuestRepository
import kotlinx.coroutines.launch

class GuestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GuestRepository
    private val eventIdLiveData = MutableLiveData<Long>()

    val guests: LiveData<List<Guest>> = eventIdLiveData.switchMap { eventId ->
        repository.getGuestsForEvent(eventId).asLiveData()
    }

    init {
        val guestDao = AppDatabase.getDatabase(application).guestDao()
        repository = GuestRepository(guestDao)
    }

    fun loadGuestsForEvent(eventId: Long) {
        eventIdLiveData.value = eventId
    }

    fun insertGuest(guest: Guest) = viewModelScope.launch {
        repository.insertGuest(guest)
    }

    fun updateGuest(guest: Guest) = viewModelScope.launch {
        repository.updateGuest(guest)
    }

    fun deleteGuest(guest: Guest) = viewModelScope.launch {
        repository.deleteGuest(guest)
    }
}