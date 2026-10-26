package com.example.eventplanner.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.database.AppDatabase
import com.example.eventplanner.data.entity.Expense
import com.example.eventplanner.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val eventIdLiveData = MutableLiveData<Long>()

    val expenses: LiveData<List<Expense>> = eventIdLiveData.switchMap { eventId ->
        repository.getExpensesForEvent(eventId).asLiveData()
    }

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

    fun loadExpensesForEvent(eventId: Long) {
        eventIdLiveData.value = eventId
    }

    fun insertExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }
}