package com.example.eventplanner.repository

import com.example.eventplanner.data.dao.ExpenseDao
import com.example.eventplanner.data.entity.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getExpensesForEvent(eventId: Long): Flow<List<Expense>> =
        expenseDao.getExpensesForEvent(eventId)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
}