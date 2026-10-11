package com.example.eventplanner.repository

import com.example.eventplanner.data.dao.ChecklistDao
import com.example.eventplanner.data.entity.ChecklistItem
import kotlinx.coroutines.flow.Flow

class ChecklistRepository(private val checklistDao: ChecklistDao) {

    fun getItemsForEvent(eventId: Long): Flow<List<ChecklistItem>> {
        return checklistDao.getItemsForEvent(eventId)
    }

    suspend fun insertItem(item: ChecklistItem): Long {
        return checklistDao.insertItem(item)
    }

    suspend fun updateItem(item: ChecklistItem) {
        checklistDao.updateItem(item)
    }

    suspend fun deleteItem(item: ChecklistItem) {
        checklistDao.deleteItem(item)
    }
}