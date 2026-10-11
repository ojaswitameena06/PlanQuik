package com.example.eventplanner.ui.checklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.database.AppDatabase
import com.example.eventplanner.data.entity.ChecklistItem
import com.example.eventplanner.repository.ChecklistRepository
import kotlinx.coroutines.launch

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChecklistRepository
    private val eventIdLiveData = MutableLiveData<Long>()

    val items: LiveData<List<ChecklistItem>> = eventIdLiveData.switchMap { eventId ->
        repository.getItemsForEvent(eventId).asLiveData()
    }

    init {
        val checklistDao = AppDatabase.getDatabase(application).checklistDao()
        repository = ChecklistRepository(checklistDao)
    }

    fun loadItemsForEvent(eventId: Long) {
        eventIdLiveData.value = eventId
    }

    fun insertItem(item: ChecklistItem) = viewModelScope.launch {
        repository.insertItem(item)
    }

    fun updateItem(item: ChecklistItem) = viewModelScope.launch {
        repository.updateItem(item)
    }

    fun deleteItem(item: ChecklistItem) = viewModelScope.launch {
        repository.deleteItem(item)
    }
}