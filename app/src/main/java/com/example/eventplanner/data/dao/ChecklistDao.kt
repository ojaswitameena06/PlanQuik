package com.example.eventplanner.data.dao

import androidx.room.*
import com.example.eventplanner.data.entity.ChecklistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Insert
    suspend fun insertItem(item: ChecklistItem): Long

    @Update
    suspend fun updateItem(item: ChecklistItem)

    @Delete
    suspend fun deleteItem(item: ChecklistItem)

    @Query("SELECT * FROM checklist_items WHERE eventId = :eventId")
    fun getItemsForEvent(eventId: Long): Flow<List<ChecklistItem>>
}